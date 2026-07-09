package supercritical.api.nuclear.fission

import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import supercritical.api.nuclear.fission.components.ControlRod
import supercritical.api.nuclear.fission.components.CoolantChannel
import supercritical.api.nuclear.fission.components.FuelRod
import supercritical.api.nuclear.fission.components.ReactorComponent
import supercritical.common.SCConfigHolder
import java.util.*
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class FissionReactor(size: Int, val reactorDepth: Int, controlRodInsertion: Double) {
    private val reactorLayout: Array<Array<ReactorComponent?>?>
    val fuelRods: MutableList<FuelRod> = ArrayList<FuelRod>()
    val controlRods: MutableList<ControlRod> = ArrayList<ControlRod>()
    val coolantChannels: MutableList<CoolantChannel> = ArrayList<CoolantChannel>()
    private val effectiveControlRods: MutableList<ControlRod?> = ArrayList<ControlRod?>()
    private val reactorRadius: Double
    private val surfaceArea: Double
    private val exteriorPressure: Double = STANDARD_PRESSURE
    private val envTemperature: Double = ROOM_TEMPERATURE

    private var k = 0.0
    private var controlRodFactor = 0.0
    private var coolantBoilingPointStandardPressure = 0.0
    private var coolantExitTemperature = 0.0
    private var coolantHeatOfVaporization = 0.0
    private var coolantBaseTemperature = 0.0
    private var prevTemperature = 0.0
    private var neutronPoisonAmount = 0.0
    private var decayProductsAmount = 0.0
    private var decayNeutrons = 0.0
    private var neutronToPowerConversion = 0.0
    private var structuralMass: Double
    private var coolantMass = 0.0
    private var weightedGenerationTime = 2.0
    var isOn: Boolean = false

    var kEff: Double = 0.0
    var controlRodInsertion: Double
    var power: Double = 0.0
    var temperature: Double = ROOM_TEMPERATURE
    var pressure: Double = STANDARD_PRESSURE
    var fuelDepletion: Double = -1.0
    var accumulatedHydrogen: Double = 0.0
    var maxTemperature: Double = 2000.0
    var maxPressure: Double = 15000000.0
    var maxPower: Double = 3.0
    var fuelMass: Double = 0.0
    var neutronFlux: Double = 0.0
    var controlRodRegulationOn: Boolean = true

    fun setComponent(x: Int, y: Int, component: ReactorComponent?) {
        reactorLayout[x]!![y] = component
    }

    fun getComponent(x: Int, y: Int): ReactorComponent? {
        return reactorLayout[x]!![y]
    }

    fun prepareThermalProperties() {
        fuelRods.clear()
        controlRods.clear()
        coolantChannels.clear()
        effectiveControlRods.clear()
        structuralMass = reactorDepth * reactorRadius * reactorRadius * Math.PI * 300
        fuelMass = 0.0
        coolantMass = 0.0
        maxTemperature = 2000.0

        var fuelIndex = 0
        var controlIndex = 0
        var coolantIndex = 0
        for (x in reactorLayout.indices) {
            for (y in reactorLayout[x]!!.indices) {
                val component = reactorLayout[x]!![y]
                if (component == null || !component.isValid()) continue
                component.setPos(x, y)
                maxTemperature = min(maxTemperature, component.getMaxTemperature())
                structuralMass += component.getMass()
                if (component is FuelRod) {
                    component.setIndex(fuelIndex++)
                    fuelRods.add(component)
                    fuelMass += component.getMass()
                } else if (component is ControlRod) {
                    component.setIndex(controlIndex++)
                    controlRods.add(component)
                } else if (component is CoolantChannel) {
                    component.setIndex(coolantIndex++)
                    component.setWeight(0.0)
                    coolantChannels.add(component)
                }
            }
        }
    }

    fun computeK(addToEffectiveLists: Boolean, controlRodsInserted: Boolean): Double {
        val geometricMatrixNeutrons = Array<DoubleArray?>(fuelRods.size) { DoubleArray(fuelRods.size) }
        val geometricMatrixFastNeutrons = Array<DoubleArray?>(fuelRods.size) { DoubleArray(fuelRods.size) }
        val geometricMatrixSlowNeutrons = Array<DoubleArray?>(fuelRods.size) { DoubleArray(fuelRods.size) }

        for (i in fuelRods.indices) {
            for (j in 0..<i) {
                var mij = 0.0
                var saij = 0.0
                var faij = 0.0
                val rodOne = fuelRods.get(i)
                val rodTwo = fuelRods.get(j)

                var prevX = fuelRods.get(i).getX()
                var prevY = fuelRods.get(i).getY()
                val resolution = SCConfigHolder.NUCLEAR.fissionReactorResolution.get().toInt()
                for (t in 0..<resolution) {
                    val x = Math.round(
                        (rodTwo.getX() - rodOne.getX()) *
                                (t.toDouble() / resolution) + fuelRods.get(i).getX()
                    ).toInt()
                    val y = Math.round(
                        (rodTwo.getY() - rodOne.getY()) *
                                (t.toDouble() / resolution) + fuelRods.get(i).getY()
                    ).toInt()
                    if (x < 0 || x > reactorLayout.size - 1 || y < 0 || y > reactorLayout.size - 1) {
                        continue
                    }
                    val component = reactorLayout[x]!![y]

                    if (component == null) {
                        continue
                    }

                    if (!component.samePositionAs(fuelRods.get(i)) &&
                        !component.samePositionAs(fuelRods.get(j))
                    ) {
                        saij += component.getAbsorptionFactor(controlRodsInserted, true)
                        faij += component.getAbsorptionFactor(controlRodsInserted, false)
                    }

                    if (component.getModerationFactor() > 0) {
                        mij += component.getModerationFactor()
                        saij = (faij + saij) / 2
                    }

                    if (!addToEffectiveLists || (x == prevX && y == prevY)) {
                        continue
                    }
                    prevX = x
                    prevY = y

                    if (component is ControlRod) {
                        component.addFuelRodPair()
                    }
                }

                mij /= resolution.toDouble()
                faij /= resolution.toDouble()
                saij /= resolution.toDouble()

                val dist = rodOne.getDistance(rodTwo)
                val unabsorbedFast = exp(-faij * dist) / dist
                val unabsorbedSlow = exp(-saij * dist) / dist
                var fast = exp(-mij * dist) / dist
                val slow = (1 / dist - fast) * unabsorbedSlow
                fast = fast * unabsorbedFast

                var slowNeutronFissionMultiplier = rodTwo.getFuel().getSlowFissionMultiplier()
                var fastNeutronFissionMultiplier = rodTwo.getFuel().getFastFissionMultiplier()
                geometricMatrixNeutrons[i]!![j] = slow * slowNeutronFissionMultiplier +
                        fast * fastNeutronFissionMultiplier

                slowNeutronFissionMultiplier = rodOne.getFuel().getSlowFissionMultiplier()
                fastNeutronFissionMultiplier = rodOne.getFuel().getFastFissionMultiplier()
                geometricMatrixNeutrons[j]!![i] = slow * slowNeutronFissionMultiplier +
                        fast * fastNeutronFissionMultiplier

                if (addToEffectiveLists) {
                    geometricMatrixFastNeutrons[i]!![j] = fast * rodTwo.getFuel().getFastNeutronCaptureCrossSection()
                    geometricMatrixSlowNeutrons[i]!![j] = slow * rodTwo.getFuel().getSlowNeutronCaptureCrossSection()

                    geometricMatrixFastNeutrons[j]!![i] = fast * rodOne.getFuel().getFastNeutronCaptureCrossSection()
                    geometricMatrixSlowNeutrons[j]!![i] = slow * rodOne.getFuel().getSlowNeutronCaptureCrossSection()
                }
            }
        }

        val vector = DoubleArray(fuelRods.size)
        Arrays.fill(vector, 1.0)
        for (i in 0..<SCConfigHolder.NUCLEAR.fissionReactorPowerIterations.get()) {
            normalize(vector)
            multiply(geometricMatrixNeutrons, vector)
        }
        var kCalc: Double = getMagnitude(vector)
        if (addToEffectiveLists) {
            linearNormalize(vector)
            for (i in fuelRods.indices) {
                fuelRods.get(i).setWeight(vector[i])
            }
            val fastVector = vector.copyOf(vector.size)
            val slowVector = vector.copyOf(vector.size)
            multiply(geometricMatrixFastNeutrons, fastVector)
            multiply(geometricMatrixSlowNeutrons, slowVector)
            for (i in fuelRods.indices) {
                if (slowVector[i] + fastVector[i] == 0.0) {
                    fuelRods.get(i).setThermalProportion(0.0)
                } else {
                    fuelRods.get(i).setThermalProportion(slowVector[i] / (slowVector[i] + fastVector[i]))
                }
            }
        }

        kCalc *= reactorDepth / (1.0 + reactorDepth)
        return kCalc
    }

    fun computeGeometry() {
        effectiveControlRods.clear()

        if (fuelRods.isEmpty()) {
            k = 0.0
            kEff = 0.0
            maxPower = 0.0
            controlRodFactor = 0.0
            prepareInitialConditions()
            return
        }

        k = computeK(true, false)
        val kExperimental = computeK(false, true)

        computeControlRodWeights(((k - 1) / k) - ((kExperimental - 1) / kExperimental))

        neutronToPowerConversion = 0.0
        decayNeutrons = 0.0

        for (rod in fuelRods) {
            neutronToPowerConversion += rod.getFuel().getReleasedHeatEnergy() / rod.getFuel().getRequiredNeutrons()
            decayNeutrons += rod.getFuel().getDecayRate()
        }
        computeCoolantWeights()

        if (fuelRods.size > 1) {
            neutronToPowerConversion /= fuelRods.size.toDouble()
            maxPower = calculateMaxPower()
        } else {
            k = 0.00001
            maxPower = 0.1 * SCConfigHolder.NUCLEAR.nuclearPowerMultiplier.get()
        }

        controlRodFactor = ControlRod.Companion.controlRodFactor(effectiveControlRods, controlRodInsertion)

        prepareInitialConditions()
    }

    private val dx = intArrayOf(0, 1, 0, -1)
    private val dy = intArrayOf(1, 0, -1, 0)

    init {
        reactorLayout = Array<Array<ReactorComponent?>?>(size) { arrayOfNulls<ReactorComponent>(size) }
        reactorRadius = size.toDouble() / 2 + 1.5
        this.controlRodInsertion = max(0.001, controlRodInsertion)
        surfaceArea = (reactorRadius * reactorRadius) * Math.PI * 2 + reactorDepth * reactorRadius * Math.PI * 2
        structuralMass = reactorDepth * reactorRadius * reactorRadius * Math.PI * 300
    }

    protected fun computeCoolantWeights() {
        for (rod in fuelRods) {
            for (i in 0..3) {
                val x = rod.getX() + dx[i]
                val y = rod.getY() + dy[i]
                if (x < 0 || x >= reactorLayout.size || y < 0 || y >= reactorLayout[x]!!.size) continue
                val comp = reactorLayout[x]!![y]
                if (comp is CoolantChannel) {
                    comp.addWeight(rod.getWeight())
                }
            }
        }
    }

    protected fun computeControlRodWeights(totalWorth: Double) {
        var totalWeight = 0.0
        for (rod in controlRods) {
            rod.computeWeightFromFuelRodMap()
            if (rod.getWeight() > 0) {
                effectiveControlRods.add(rod)
                totalWeight += rod.getWeight()
            }
        }
        ControlRod.Companion.normalizeWeights(effectiveControlRods, totalWeight, totalWorth)
    }

    fun resetFuelDepletion() {
        this.fuelDepletion = 0.0
    }

    fun prepareInitialConditions() {
        coolantBaseTemperature = 0.0
        coolantBoilingPointStandardPressure = 0.0
        coolantExitTemperature = 0.0
        coolantHeatOfVaporization = 0.0
        weightedGenerationTime = 0.0

        for (rod in fuelRods) {
            weightedGenerationTime += rod.getNeutronGenerationTime()
        }
        if (fuelRods.isEmpty()) {
            weightedGenerationTime = 2.0
        } else {
            weightedGenerationTime /= fuelRods.size.toDouble()
        }

        for (channel in coolantChannels) {
            val prop = channel.getCoolant()
            val original = CoolantRegistry.originalFluid(prop)

            if (original != null) {
                coolantBaseTemperature += original.getFluidType().getTemperature().toDouble()
            }
            coolantBoilingPointStandardPressure += prop.getBoilingPoint()
            coolantExitTemperature += prop.getHotCoolant().getFluidType().getTemperature().toDouble()
            coolantHeatOfVaporization += prop.getHeatOfVaporization()
        }

        if (!coolantChannels.isEmpty()) {
            coolantBaseTemperature /= coolantChannels.size.toDouble()
            coolantBoilingPointStandardPressure /= coolantChannels.size.toDouble()
            coolantExitTemperature /= coolantChannels.size.toDouble()
            coolantHeatOfVaporization /= coolantChannels.size.toDouble()

            if (coolantBaseTemperature == 0.0) {
                coolantBaseTemperature = envTemperature
            }
            if (coolantBoilingPointStandardPressure == 0.0) {
                coolantBoilingPointStandardPressure = AIR_BOILING_POINT
            }
        }
        this.isOn = true
    }

    fun makeCoolantFlow(): Double {
        var heatRemoved = 0.0
        coolantMass = 0.0
        for (channel in coolantChannels) {
            val input = channel.getInputHandler()
            val output = channel.getOutputHandler()
            if (input == null || output == null) continue

            val inputTank = input.getFluidTank()
            val outputTank = output.getFluidTank()
            val drained = inputTank.drain(16000, FluidAction.SIMULATE)
            if (drained.isEmpty()) continue

            val available = drained.getAmount()
            val prop = channel.getCoolant()
            val original = CoolantRegistry.originalFluid(prop)
            val coolantTemp =
                if (original == null) ROOM_TEMPERATURE.toInt() else original.getFluidType().getTemperature()
            val hotCoolant = prop.getHotCoolant()
            val cooledTemperature =
                if (hotCoolant == null) ROOM_TEMPERATURE.toInt() else hotCoolant.getFluidType().getTemperature()
            if (cooledTemperature > this.temperature) continue

            val heatRemovedPerLiter = prop.getSpecificHeatCapacity() /
                    SCConfigHolder.NUCLEAR.fissionCoolantDivisor.get() *
                    (cooledTemperature - coolantTemp)
            if (heatRemovedPerLiter <= 0) continue

            val heatFluxPerAreaAndTemp: Double = 1 /
                    (1 / prop.getCoolingFactor() + coolantWallThickness / thermalConductivity)
            val idealHeatFlux = heatFluxPerAreaAndTemp * channel.getWeight() * reactorDepth *
                    (temperature - cooledTemperature)

            val idealFluidUsed = idealHeatFlux / heatRemovedPerLiter
            val cappedFluidUsed = min(available.toDouble(), idealFluidUsed)

            val remainingSpace = outputTank.getTankCapacity(0) - outputTank.getFluidInTank(0).getAmount()
            val actualFlowRate = min(
                remainingSpace,
                (cappedFluidUsed + channel.partialCoolant).toInt()
            )
            channel.partialCoolant += cappedFluidUsed - actualFlowRate

            val hotFluid = FluidStack(hotCoolant, actualFlowRate)
            inputTank.drain(actualFlowRate, FluidAction.EXECUTE)
            outputTank.fill(hotFluid, FluidAction.EXECUTE)

            if (prop.accumulatesHydrogen() &&
                this.temperature > zircaloyHydrogenReactionTemperature
            ) {
                val boilingPoint = coolantBoilingPoint(prop)
                if (this.temperature > boilingPoint) {
                    this.accumulatedHydrogen += (this.temperature - boilingPoint) / boilingPoint
                } else if (actualFlowRate < min(remainingSpace.toDouble(), idealFluidUsed)) {
                    this.accumulatedHydrogen += (this.temperature - zircaloyHydrogenReactionTemperature) /
                            zircaloyHydrogenReactionTemperature
                }
            }

            this.coolantMass += cappedFluidUsed * prop.getMass()
            heatRemoved += cappedFluidUsed * heatRemovedPerLiter
        }
        this.coolantMass /= 1000.0
        this.accumulatedHydrogen *= 0.98
        return heatRemoved
    }

    fun calculateMaxPower(): Double {
        val hypotheticalTemperature = min(maxTemperature, zircaloyHydrogenReactionTemperature)
        var heatRemoved = 0.0
        for (channel in coolantChannels) {
            val prop = channel.getCoolant()
            val original = CoolantRegistry.originalFluid(prop)
            val coolantTemp =
                if (original == null) ROOM_TEMPERATURE.toInt() else original.getFluidType().getTemperature()

            val hotCoolant = prop.getHotCoolant()
            val cooledTemperature =
                if (hotCoolant == null) ROOM_TEMPERATURE.toInt() else hotCoolant.getFluidType().getTemperature()
            if (cooledTemperature > hypotheticalTemperature) {
                continue
            }

            val heatRemovedPerLiter = prop.getSpecificHeatCapacity() /
                    SCConfigHolder.NUCLEAR.fissionCoolantDivisor.get() *
                    (cooledTemperature - coolantTemp)

            val heatFluxPerAreaAndTemp: Double = 1 /
                    (1 / prop.getCoolingFactor() + coolantWallThickness / thermalConductivity)
            val idealHeatFlux = heatFluxPerAreaAndTemp * channel.getWeight() * reactorDepth *
                    (hypotheticalTemperature - cooledTemperature)

            val idealFluidUsed = idealHeatFlux / heatRemovedPerLiter

            heatRemoved += idealFluidUsed * heatRemovedPerLiter
        }
        val timeConstant: Double = specificHeatCapacity *
                (1 / convectiveHeatTransferCoefficient + wallThickness / thermalConductivity) / this.surfaceArea

        return ((hypotheticalTemperature - envTemperature) * (timeConstant * (this.coolantMass +
                this.structuralMass + this.fuelMass)) + heatRemoved) / 1e6
    }

    protected fun coolantBoilingPoint(): Double {
        return this.coolantBoilingPointStandardPressure
    }

    protected fun coolantBoilingPoint(coolant: ICoolantStats): Double {
        if (coolant.getBoilingPoint() == 0.0) {
            return coolantBoilingPoint()
        }
        return coolant.getBoilingPoint()
    }

    fun updateTemperature() {
        this.prevTemperature = this.temperature
        this.temperature = responseFunctionTemperature(envTemperature, this.temperature, this.power * 1e6, 0.0)
        this.temperature = min(maxTemperature, temperature)
        val heatRemoved = this.makeCoolantFlow()
        this.temperature = responseFunctionTemperature(envTemperature, prevTemperature, this.power * 1e6, heatRemoved)
        this.temperature = max(this.temperature, this.coolantBaseTemperature)
    }

    fun updatePressure() {
        this.pressure = responseFunction(
            if (!(this.temperature <= this.coolantBoilingPoint()) && this.isOn) 1000.0 * R * this.temperature else this.exteriorPressure,
            this.pressure, 0.2
        )
    }

    fun updateNeutronPoisoning() {
        this.decayProductsAmount *= decayProductRate
        this.neutronPoisonAmount += this.decayProductsAmount * (1 - decayProductRate) * poisonFraction
        this.neutronPoisonAmount *= decayProductRate * exp(-crossSectionRatio * power / surfaceArea)
    }

    val totalDecayNeutrons: Double
        get() = this.neutronPoisonAmount * 0.05 + this.decayProductsAmount * 0.1 + this.decayNeutrons

    fun updatePower() {
        if (this.isOn) {
            this.neutronFlux += this.totalDecayNeutrons
            this.kEff =
                1 / ((1 / this.k) + powerDefectCoefficient * (this.power / this.maxPower) + neutronPoisonAmount * crossSectionRatio / surfaceArea + controlRodFactor)
            this.kEff = max(0.0, this.kEff)

            val inverseReactorPeriod = (this.kEff - 1) / weightedGenerationTime

            this.neutronFlux *= exp(inverseReactorPeriod)

            this.fuelDepletion += this.neutronFlux * reactorDepth
            this.decayProductsAmount += max(neutronFlux, 0.0) / 250000

            this.power = this.neutronFlux * this.neutronToPowerConversion
        } else {
            this.neutronFlux *= 0.5
            this.power *= 0.5
        }
    }

    fun checkForMeltdown(): Boolean {
        return this.temperature > this.maxTemperature
    }

    fun checkForExplosion(): Boolean {
        return this.pressure > this.maxPressure
    }

    fun tick() {
        if (!this.isOn || fuelRods.isEmpty()) return
        updatePower()
        updateTemperature()
        updatePressure()
        updateNeutronPoisoning()
        regulateControlRods()
    }

    protected fun responseFunctionTemperature(
        envTemperature: Double, currentTemperature: Double, heatAdded: Double,
        heatAbsorbed: Double
    ): Double {
        var currentTemperature = currentTemperature
        var heatAbsorbed = heatAbsorbed
        currentTemperature = max(0.1, currentTemperature)
        heatAbsorbed = max(0.0, heatAbsorbed)
        val timeConstant: Double = specificHeatCapacity *
                (1 / convectiveHeatTransferCoefficient + wallThickness / thermalConductivity) / this.surfaceArea
        val expDecay = exp(-timeConstant)
        val effectiveEnvTemperature = envTemperature +
                (heatAdded - heatAbsorbed) / (timeConstant * (this.coolantMass + this.structuralMass + this.fuelMass))
        return currentTemperature * expDecay + effectiveEnvTemperature * (1 - expDecay)
    }

    fun updateControlRodInsertion(controlRodInsertion: Double) {
        this.controlRodInsertion = max(0.001, controlRodInsertion)
        this.controlRodFactor = ControlRod.Companion.controlRodFactor(effectiveControlRods, this.controlRodInsertion)
    }

    fun regulateControlRods() {
        if (!this.isOn || !this.controlRodRegulationOn) return

        var adjustFactor = false
        if (pressure > maxPressure * 0.8 || temperature > (coolantExitTemperature + maxTemperature) / 2 || temperature > maxTemperature - 150 || temperature - prevTemperature > 30) {
            if (kEff > 0.99) {
                this.controlRodInsertion += 0.004
                adjustFactor = true
            }
        } else if (temperature > coolantExitTemperature * 0.3 + coolantBaseTemperature * 0.7) {
            if (kEff > 1.01) {
                this.controlRodInsertion += 0.008
                adjustFactor = true
            } else if (kEff < 1.005) {
                this.controlRodInsertion -= 0.001
                adjustFactor = true
            }
        } else if (temperature > coolantExitTemperature * 0.1 + coolantBaseTemperature * 0.9) {
            if (kEff > 1.025) {
                this.controlRodInsertion += 0.012
                adjustFactor = true
            } else if (kEff < 1.015) {
                this.controlRodInsertion -= 0.004
                adjustFactor = true
            }
        } else {
            if (kEff > 1.1) {
                this.controlRodInsertion += 0.02
                adjustFactor = true
            } else if (kEff < 1.05) {
                this.controlRodInsertion -= 0.006
                adjustFactor = true
            }
        }

        if (adjustFactor) {
            this.controlRodInsertion = max(0.0, min(1.0, this.controlRodInsertion))
            this.controlRodFactor =
                ControlRod.Companion.controlRodFactor(effectiveControlRods, this.controlRodInsertion)
        }
    }

    fun turnOff() {
        this.isOn = false
        this.maxPower = 0.0
        this.k = 0.0
        this.kEff = 0.0
        this.coolantMass = 0.0
        this.fuelMass = 0.0
        for (components in reactorLayout) {
            Arrays.fill(components, null)
        }
        fuelRods.clear()
        controlRods.clear()
        coolantChannels.clear()
        effectiveControlRods.clear()
    }

    companion object {
        const val R: Double = 8.31446261815324
        const val STANDARD_PRESSURE: Double = 101325.0
        const val ROOM_TEMPERATURE: Double = 273.0
        const val AIR_BOILING_POINT: Double = 78.8

        var thermalConductivity: Double = 45.0 // W/(m K), for steel
        var wallThickness: Double = 0.1 // m
        var coolantWallThickness: Double = 0.06 // m
        var specificHeatCapacity: Double = 420.0 // J/(kg K), for steel
        var convectiveHeatTransferCoefficient: Double = 10.0 // W/(m^2 K), for slow-moving air
        var powerDefectCoefficient: Double = 0.016 // reactivity units
        var decayProductRate: Double =
            0.997 // based on the half-life of xenon-135, using real-life days as Minecraft days
        var poisonFraction: Double = 0.063 // xenon-135 yield from fission
        var crossSectionRatio: Double = 4.0 // ratio between the cross section for typical fuels and xenon-135
        var zircaloyHydrogenReactionTemperature: Double = 1500.0 // K

        protected fun responseFunction(target: Double, current: Double, criticalRate: Double): Double {
            var current = current
            if (current < 0) current = if (criticalRate < 1) 0.0 else 0.1
            val expDecay = exp(-criticalRate)
            return current * expDecay + target * (1 - expDecay)
        }

        fun getMagnitude(vector: DoubleArray): Double {
            var magnitude = 0.0
            for (component in vector) magnitude += component * component
            return sqrt(magnitude)
        }

        fun normalize(vector: DoubleArray) {
            val magnitude: Double = getMagnitude(vector)
            if (magnitude == 0.0) return
            for (i in vector.indices) vector[i] /= magnitude
        }

        fun linearNormalize(vector: DoubleArray) {
            var sum = 0.0
            for (component in vector) sum += component
            if (sum == 0.0) return
            for (i in vector.indices) vector[i] /= sum
        }

        fun multiply(matrix: Array<DoubleArray?>, vector: DoubleArray) {
            val result = DoubleArray(vector.size)
            for (i in matrix.indices) {
                for (j in matrix[i]!!.indices) {
                    result[i] += matrix[i]!![j] * vector[j]
                }
            }
            System.arraycopy(result, 0, vector, 0, result.size)
        }
    }
}
