package io.github.symmetricdevs.supercritical.api.nuclear.fission

import net.minecraft.nbt.CompoundTag
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.ControlRod
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.CoolantChannel
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.FuelRod
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.ReactorComponent
import io.github.symmetricdevs.supercritical.config.ScritConfig
import java.util.*
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class FissionReactor(size: Int, val reactorDepth: Int, controlRodInsertion: Double) {
    // ----- structure -----
    private val reactorLayout: Array<Array<ReactorComponent?>>
    private val reactorRadius: Double
    private val surfaceArea: Double
    private val exteriorPressure: Double = STANDARD_PRESSURE
    private val envTemperature: Double = ROOM_TEMPERATURE

    // ----- component collections -----
    val fuelRods: MutableList<FuelRod> = arrayListOf()
    val controlRods: MutableList<ControlRod> = arrayListOf()
    val coolantChannels: MutableList<CoolantChannel> = arrayListOf()
    private val effectiveControlRods: MutableList<ControlRod> = arrayListOf()

    // ----- mutable physics state -----
    private var k = 0.0
    var kEff: Double = 0.0
    private var controlRodFactor = 0.0
    private var neutronToPowerConversion = 0.0
    private var decayNeutrons = 0.0
    private var neutronPoisonAmount = 0.0
    private var decayProductsAmount = 0.0
    private var weightedGenerationTime = 2.0
    var neutronFlux: Double = 0.0
    var power: Double = 0.0
    var temperature: Double = ROOM_TEMPERATURE
    private var prevTemperature = 0.0
    var pressure: Double = STANDARD_PRESSURE
    var fuelDepletion: Double = -1.0
    var accumulatedHydrogen: Double = 0.0

    // ----- coolant-derived -----
    private var coolantBaseTemperature = 0.0
    private var coolantBoilingPointStandardPressure = 0.0
    private var coolantExitTemperature = 0.0
    private var coolantHeatOfVaporization = 0.0
    private var coolantMass = 0.0
    private var structuralMass: Double
    var fuelMass: Double = 0.0

    // ----- limits / config -----
    var maxTemperature: Double = 2000.0
    var maxPressure: Double = 15000000.0
    var maxPower: Double = 3.0
    var controlRodInsertion: Double
    var controlRodRegulationOn: Boolean = true
    var isOn: Boolean = false

    init {
        reactorLayout = Array(size) { arrayOfNulls(size) }
        reactorRadius = size.toDouble() / 2 + 1.5
        this.controlRodInsertion = max(0.001, controlRodInsertion)
        surfaceArea = (reactorRadius * reactorRadius) * Math.PI * 2 + reactorDepth * reactorRadius * Math.PI * 2
        structuralMass = reactorDepth * reactorRadius * reactorRadius * Math.PI * 300
    }

    // ===================== layout + prepareThermalProperties =====================

    fun setComponent(x: Int, y: Int, component: ReactorComponent?) {
        reactorLayout[x][y] = component
    }

    fun getComponent(x: Int, y: Int): ReactorComponent? {
        return reactorLayout[x][y]
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
            for (y in reactorLayout[x].indices) {
                val component = reactorLayout[x][y]
                if (component == null || !component.isValid) continue
                component.setPos(x, y)
                maxTemperature = min(maxTemperature, component.maxTemperature)
                structuralMass += component.mass
                if (component is FuelRod) {
                    component.index = fuelIndex++
                    fuelRods.add(component)
                } else if (component is ControlRod) {
                    component.index = controlIndex++
                    controlRods.add(component)
                } else if (component is CoolantChannel) {
                    component.index = coolantIndex++
                    component.weight = 0.0
                    coolantChannels.add(component)
                }
            }
        }
    }

    fun prepareInitialConditions() {
        coolantBaseTemperature = 0.0
        coolantBoilingPointStandardPressure = 0.0
        coolantExitTemperature = 0.0
        coolantHeatOfVaporization = 0.0
        weightedGenerationTime = 0.0

        for (rod in fuelRods) {
            weightedGenerationTime += rod.neutronGenerationTime
        }
        if (fuelRods.isEmpty()) {
            weightedGenerationTime = 2.0
        } else {
            weightedGenerationTime /= fuelRods.size.toDouble()
        }

        for (channel in coolantChannels) {
            val prop = channel.coolant
            val original = CoolantRegistry.originalFluid(prop)
            val hotCoolant = requireNotNull(prop.hotCoolant) { "Coolant must define a hot coolant fluid" }

            if (original != null) {
                coolantBaseTemperature += original.getFluidType().temperature.toDouble()
            }
            coolantBoilingPointStandardPressure += prop.boilingPoint
            coolantExitTemperature += hotCoolant.getFluidType().temperature.toDouble()
            coolantHeatOfVaporization += prop.heatOfVaporization
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

    fun resetFuelDepletion() {
        this.fuelDepletion = 0.0
    }

    // ===================== geometry / eigenvalue =====================

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
            neutronToPowerConversion += rod.getFuel().releasedHeatEnergy / rod.getFuel().requiredNeutrons
            decayNeutrons += rod.getFuel().decayRate
        }
        computeCoolantWeights()

        if (fuelRods.size > 1) {
            neutronToPowerConversion /= fuelRods.size.toDouble()
            maxPower = calculateMaxPower()
        } else {
            k = 0.00001
            maxPower = 0.1 * ScritConfig.INSTANCE.nuclear.nuclearPowerMultiplier
        }

        controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, controlRodInsertion)

        prepareInitialConditions()
    }

    fun computeK(addToEffectiveLists: Boolean, controlRodsInserted: Boolean): Double {
        val neutrons = Array(fuelRods.size) { DoubleArray(fuelRods.size) }
        val fast = Array(fuelRods.size) { DoubleArray(fuelRods.size) }
        val slow = Array(fuelRods.size) { DoubleArray(fuelRods.size) }
        fillGeometricMatrices(neutrons, fast, slow, addToEffectiveLists, controlRodsInserted)
        val vector = runPowerIteration(neutrons)
        val kCalc = getMagnitude(vector)
        if (addToEffectiveLists) {
            assignRodWeightsAndThermalProportions(vector, fast, slow)
        }
        return applyLeakageFactor(kCalc)
    }

    private fun fillGeometricMatrices(
        neutrons: Array<DoubleArray>,
        fast: Array<DoubleArray>,
        slow: Array<DoubleArray>,
        addToEffectiveLists: Boolean,
        controlRodsInserted: Boolean
    ) {
        for (i in fuelRods.indices) {
            for (j in 0..<i) {
                var moderation = 0.0
                var slowAbsorption = 0.0
                var fastAbsorption = 0.0
                val rodOne = fuelRods[i]
                val rodTwo = fuelRods[j]

                var prevX = rodOne.x
                var prevY = rodOne.y
                val resolution = ScritConfig.INSTANCE.nuclear.fissionReactorResolution
                for (t in 0 until Math.ceil(resolution).toInt()) {
                    val x = Math.round(
                        (rodTwo.x - rodOne.x) *
                                (t.toDouble() / resolution) + fuelRods[i].x
                    ).toInt()
                    val y = Math.round(
                        (rodTwo.y - rodOne.y) *
                                (t.toDouble() / resolution) + fuelRods[i].y
                    ).toInt()
                    if (x < 0 || x > reactorLayout.size - 1 || y < 0 || y > reactorLayout.size - 1) {
                        continue
                    }
                    val component = reactorLayout[x][y]

                    if (component == null) {
                        continue
                    }

                    if (!component.samePositionAs(fuelRods[i]) &&
                        !component.samePositionAs(fuelRods[j])
                    ) {
                        slowAbsorption += component.getAbsorptionFactor(controlRodsInserted, true)
                        fastAbsorption += component.getAbsorptionFactor(controlRodsInserted, false)
                    }

                    if (component.moderationFactor > 0) {
                        moderation += component.moderationFactor
                        slowAbsorption = (fastAbsorption + slowAbsorption) / 2
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

                moderation /= resolution
                fastAbsorption /= resolution
                slowAbsorption /= resolution

                val dist = rodOne.getDistance(rodTwo)
                val unabsorbedFast = exp(-fastAbsorption * dist) / dist
                val unabsorbedSlow = exp(-slowAbsorption * dist) / dist
                var fastFlux = exp(-moderation * dist) / dist
                val slowFlux = (1 / dist - fastFlux) * unabsorbedSlow
                fastFlux = fastFlux * unabsorbedFast

                var slowNeutronFissionMultiplier = rodTwo.getFuel().slowFissionMultiplier
                var fastNeutronFissionMultiplier = rodTwo.getFuel().fastFissionMultiplier
                neutrons[i][j] = slowFlux * slowNeutronFissionMultiplier +
                        fastFlux * fastNeutronFissionMultiplier

                slowNeutronFissionMultiplier = rodOne.getFuel().slowFissionMultiplier
                fastNeutronFissionMultiplier = rodOne.getFuel().fastFissionMultiplier
                neutrons[j][i] = slowFlux * slowNeutronFissionMultiplier +
                        fastFlux * fastNeutronFissionMultiplier

                if (addToEffectiveLists) {
                    fast[i][j] = fastFlux * rodTwo.getFuel().fastNeutronCaptureCrossSection
                    slow[i][j] = slowFlux * rodTwo.getFuel().slowNeutronCaptureCrossSection

                    fast[j][i] = fastFlux * rodOne.getFuel().fastNeutronCaptureCrossSection
                    slow[j][i] = slowFlux * rodOne.getFuel().slowNeutronCaptureCrossSection
                }
            }
        }
    }

    private fun runPowerIteration(matrix: Array<DoubleArray>): DoubleArray {
        val vector = DoubleArray(fuelRods.size)
        Arrays.fill(vector, 1.0)
        for (i in 0..<ScritConfig.INSTANCE.nuclear.fissionReactorPowerIterations) {
            normalize(vector)
            multiply(matrix, vector)
        }
        return vector
    }

    private fun assignRodWeightsAndThermalProportions(
        vector: DoubleArray,
        fastMatrix: Array<DoubleArray>,
        slowMatrix: Array<DoubleArray>
    ) {
        linearNormalize(vector)
        for (i in fuelRods.indices) {
            fuelRods[i].weight = vector[i]
        }
        val fastVector = vector.copyOf(vector.size)
        val slowVector = vector.copyOf(vector.size)
        multiply(fastMatrix, fastVector)
        multiply(slowMatrix, slowVector)
        for (i in fuelRods.indices) {
            if (slowVector[i] + fastVector[i] == 0.0) {
                fuelRods[i].thermalProportion = 0.0
            } else {
                fuelRods[i].thermalProportion = (slowVector[i] / (slowVector[i] + fastVector[i]))
            }
        }
    }

    private fun applyLeakageFactor(kCalc: Double): Double {
        val leakageFactor = reactorDepth / (1.0 + reactorDepth)
        return kCalc * leakageFactor
    }

    protected fun computeCoolantWeights() {
        for (rod in fuelRods) {
            for (i in 0..3) {
                val x = rod.x + dx[i]
                val y = rod.y + dy[i]
                if (x < 0 || x >= reactorLayout.size || y < 0 || y >= reactorLayout[x].size) continue
                val comp = reactorLayout[x][y]
                if (comp is CoolantChannel) {
                    comp.addWeight(rod.weight)
                }
            }
        }
    }

    protected fun computeControlRodWeights(totalWorth: Double) {
        var totalWeight = 0.0
        for (rod in controlRods) {
            rod.computeWeightFromFuelRodMap()
            if (rod.weight > 0) {
                effectiveControlRods.add(rod)
                totalWeight += rod.weight
            }
        }
        ControlRod.normalizeWeights(effectiveControlRods, totalWeight, totalWorth)
    }

    fun calculateMaxPower(): Double {
        val hypotheticalTemperature = min(maxTemperature, zircaloyHydrogenReactionTemperature)
        var heatRemoved = 0.0
        for (channel in coolantChannels) {
            val prop = channel.coolant
            val coolantTemp = coolantInletTemp(prop)

            val hotCoolant = requireNotNull(prop.hotCoolant) {
                "Coolant must define a hot coolant fluid for maximum-power calculation"
            }
            val cooledTemperature = hotCoolant.getFluidType().temperature
            if (cooledTemperature > hypotheticalTemperature) {
                continue
            }

            val heatRemovedPerLiter = heatRemovedPerLiter(prop, coolantTemp, cooledTemperature)
            val idealFluidUsed =
                idealCoolantHeatFlux(prop, channel.weight, hypotheticalTemperature, cooledTemperature) / heatRemovedPerLiter

            heatRemoved += idealFluidUsed * heatRemovedPerLiter
        }
        val timeConstant = thermalTimeConstant()

        return ((hypotheticalTemperature - envTemperature) * (timeConstant * (this.coolantMass +
                this.structuralMass + this.fuelMass)) + heatRemoved) / 1e6
    }

    // ===================== coolant flow =====================

    fun makeCoolantFlow(): Double {
        var heatRemoved = 0.0
        coolantMass = 0.0
        for (channel in coolantChannels) {
            val input = channel.inputHandler
            val output = channel.outputHandler
            if (input == null || output == null) {
                continue
            }

            val inputTank = input.fluidTank
            val outputTank = output.fluidTank
            val drained = inputTank.drain(16000, FluidAction.SIMULATE)
            if (drained.isEmpty) {
                continue
            }

            val available = drained.amount
            val prop = channel.coolant
            val coolantTemp = coolantInletTemp(prop)
            val hotCoolant = requireNotNull(prop.hotCoolant) {
                "Coolant must define a hot coolant fluid for coolant flow"
            }
            val cooledTemperature = hotCoolant.getFluidType().temperature
            if (cooledTemperature > this.temperature) {
                continue
            }

            val heatRemovedPerLiter = heatRemovedPerLiter(prop, coolantTemp, cooledTemperature)
            if (heatRemovedPerLiter <= 0) {
                continue
            }

            val idealFluidUsed =
                idealCoolantHeatFlux(prop, channel.weight, this.temperature, cooledTemperature) / heatRemovedPerLiter
            val cappedFluidUsed = min(available.toDouble(), idealFluidUsed)

            val remainingSpace = outputTank.getTankCapacity(0) - outputTank.getFluidInTank(0).amount
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

            this.coolantMass += cappedFluidUsed * prop.mass
            heatRemoved += cappedFluidUsed * heatRemovedPerLiter
        }
        this.coolantMass /= 1000.0
        this.accumulatedHydrogen *= 0.98
        return heatRemoved
    }

    protected fun coolantBoilingPoint(): Double {
        return this.coolantBoilingPointStandardPressure
    }

    protected fun coolantBoilingPoint(coolant: ICoolantStats): Double {
        if (coolant.boilingPoint == 0.0) {
            return coolantBoilingPoint()
        }
        return coolant.boilingPoint
    }

    private fun coolantInletTemp(prop: ICoolantStats): Int {
        val original = CoolantRegistry.originalFluid(prop)
        return if (original == null) ROOM_TEMPERATURE.toInt() else original.getFluidType().temperature
    }

    private fun heatRemovedPerLiter(prop: ICoolantStats, coolantTemp: Int, cooledTemperature: Int): Double =
        prop.specificHeatCapacity / ScritConfig.INSTANCE.nuclear.fissionCoolantDivisor * (cooledTemperature - coolantTemp)

    private fun coolantHeatFluxPerArea(prop: ICoolantStats): Double =
        1 / (1 / prop.coolingFactor + coolantWallThickness / thermalConductivity)

    private fun idealCoolantHeatFlux(
        prop: ICoolantStats, weight: Double, refTemp: Double, cooledTemperature: Int
    ): Double =
        coolantHeatFluxPerArea(prop) * weight * reactorDepth * (refTemp - cooledTemperature)

    private fun thermalTimeConstant(): Double =
        specificHeatCapacity * (1 / convectiveHeatTransferCoefficient + wallThickness / thermalConductivity) / this.surfaceArea

    // ===================== tick updates =====================

    fun tick() {
        if (!this.isOn || fuelRods.isEmpty()) return
        updatePower()
        updateTemperature()
        updatePressure()
        updateNeutronPoisoning()
        regulateControlRods()
    }

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
                ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion)
        }
    }

    protected fun responseFunctionTemperature(
        envTemperature: Double, currentTemperature: Double, heatAdded: Double,
        heatAbsorbed: Double
    ): Double {
        var currentTemperature = currentTemperature
        var heatAbsorbed = heatAbsorbed
        currentTemperature = max(0.1, currentTemperature)
        heatAbsorbed = max(0.0, heatAbsorbed)
        val timeConstant = thermalTimeConstant()
        val expDecay = exp(-timeConstant)
        val effectiveEnvTemperature = envTemperature +
                (heatAdded - heatAbsorbed) / (timeConstant * (this.coolantMass + this.structuralMass + this.fuelMass))
        return currentTemperature * expDecay + effectiveEnvTemperature * (1 - expDecay)
    }

    // ===================== safety =====================

    fun checkForMeltdown(): Boolean {
        return this.temperature > this.maxTemperature
    }

    fun checkForExplosion(): Boolean {
        return this.pressure > this.maxPressure
    }

    fun updateControlRodInsertion(controlRodInsertion: Double) {
        this.controlRodInsertion = max(0.001, controlRodInsertion)
        this.controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion)
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

    // ===================== serialization =====================

    /**
     * Faithful port of legacy FissionReactor#serializeNBT. Persists the dynamic reactor state
     * (including the five fields the modern port previously dropped: NeutronFlux, PrevTemperature,
     * NeutronPoisonAmount, DecayProductsAmount, ControlRodRegulationOn) so a reload resumes the
     * neutron/thermal transient instead of resetting it. Structural limits (maxTemperature etc.)
     * are deliberately NOT persisted, matching legacy: they are recomputed in prepareThermalProperties.
     */
    fun serializeNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putDouble("Temperature", temperature)
        tag.putDouble("PrevTemperature", prevTemperature)
        tag.putDouble("Pressure", pressure)
        tag.putDouble("Power", power)
        tag.putDouble("NeutronFlux", neutronFlux)
        tag.putDouble("FuelDepletion", fuelDepletion)
        tag.putDouble("AccumulatedHydrogen", accumulatedHydrogen)
        tag.putDouble("NeutronPoisonAmount", neutronPoisonAmount)
        tag.putDouble("DecayProductsAmount", decayProductsAmount)
        tag.putDouble("ControlRodInsertion", controlRodInsertion)
        tag.putBoolean("IsOn", isOn)
        tag.putBoolean("ControlRodRegulationOn", controlRodRegulationOn)
        return tag
    }

    /**
     * Faithful port of legacy FissionReactor#deserializeNBT. Restores the fields written by
     * [serializeNBT]. Should be called after [computeGeometry] so structural properties are fresh.
     */
    fun deserializeNBT(tag: CompoundTag) {
        temperature = tag.getDouble("Temperature")
        prevTemperature = tag.getDouble("PrevTemperature")
        pressure = tag.getDouble("Pressure")
        power = tag.getDouble("Power")
        neutronFlux = tag.getDouble("NeutronFlux")
        fuelDepletion = tag.getDouble("FuelDepletion")
        accumulatedHydrogen = tag.getDouble("AccumulatedHydrogen")
        neutronPoisonAmount = tag.getDouble("NeutronPoisonAmount")
        decayProductsAmount = tag.getDouble("DecayProductsAmount")
        controlRodInsertion = tag.getDouble("ControlRodInsertion")
        isOn = tag.getBoolean("IsOn")
        controlRodRegulationOn = tag.getBoolean("ControlRodRegulationOn")
    }

    companion object {
        const val R: Double = 8.31446261815324
        const val STANDARD_PRESSURE: Double = 101325.0
        const val ROOM_TEMPERATURE: Double = 273.0
        const val AIR_BOILING_POINT: Double = 78.8

        private val dx = intArrayOf(0, 1, 0, -1)
        private val dy = intArrayOf(1, 0, -1, 0)

        var thermalConductivity: Double = 45.0 // W/(m K), for steel
        var wallThickness: Double = 0.1 // m
        var coolantWallThickness: Double = 0.02 // m (legacy value; 0.06 was the original, then /3 for balance)
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
            if (current < 0) {
                if (criticalRate < 1) return 0.0
                current = 0.1
            }
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

        fun multiply(matrix: Array<DoubleArray>, vector: DoubleArray) {
            val result = DoubleArray(vector.size)
            for (i in matrix.indices) {
                for (j in matrix[i].indices) {
                    result[i] += matrix[i][j] * vector[j]
                }
            }
            System.arraycopy(result, 0, vector, 0, result.size)
        }
    }
}
