package io.github.symmetricdevs.supercritical.api.nuclear.fission

import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.ControlRod
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.CoolantChannel
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.FuelRod
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.ReactorComponent
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.*
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.control.LegacyControlRodBank
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.families.LegacyPWRFamily
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.geometry.SquareLattice
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.pwr.LegacyEigenvalueNeutronics
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.pwr.LegacyPWRThermalHydraulics
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.pwr.SolidRodFuelCycle
import net.minecraft.nbt.CompoundTag
import java.util.*
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class FissionReactor(size: Int, val reactorDepth: Int, controlRodInsertion: Double) : ReactorCore {
    // ----- structure -----
    internal val reactorLayout: Array<Array<ReactorComponent?>> = Array(size) { arrayOfNulls(size) }
    private val reactorRadius: Double = size.toDouble() / 2 + 1.5
    internal val surfaceArea: Double
    internal val exteriorPressure: Double = STANDARD_PRESSURE
    internal val envTemperature: Double = ROOM_TEMPERATURE

    // ----- ReactorCore API -----
    override val family = LegacyPWRFamily
    private val squareLattice = SquareLattice(size, reactorDepth, reactorLayout)
    override val geometry: ReactorGeometry get() = squareLattice
    override val state: ReactorState
        get() = ReactorState(
            neutronFlux = neutronFlux,
            power = power,
            temperature = temperature,
            prevTemperature = prevTemperature,
            pressure = pressure,
            fuelDepletion = fuelDepletion,
            accumulatedHydrogen = accumulatedHydrogen,
            neutronPoisonAmount = neutronPoisonAmount,
            decayProductsAmount = decayProductsAmount,
            isOn = isOn
        )
    override val limits: ReactorLimits
        get() = ReactorLimits(
            maxTemperature = this@FissionReactor.maxTemperature,
            maxPressure = this@FissionReactor.maxPressure,
            maxPower = this@FissionReactor.maxPower
        )
    override val control: ControlMechanism = LegacyControlRodBank(this)

    // ----- component collections -----
    val fuelRods: MutableList<FuelRod> = arrayListOf()
    val controlRods: MutableList<ControlRod> = arrayListOf()
    val coolantChannels: MutableList<CoolantChannel> = arrayListOf()
    internal val effectiveControlRods: MutableList<ControlRod> = arrayListOf()

    // ----- physics kernels (internal collaborators; this class is still the ReactorCore) -----
    // Typed against the kernel abstractions where only interface methods are used; the thermal
    // kernel is referenced by its concrete type so updateTemperature/updatePressure can reach its
    // step methods. The legacy formulas live in the kernels; FissionReactor remains the legacy PWR
    // core ("re-implement, not wrap").
    private val neutronics: NeutronicsKernel = LegacyEigenvalueNeutronics(this)
    private val thermal: LegacyPWRThermalHydraulics = LegacyPWRThermalHydraulics(this)
    private val fuelCycle: FuelCycle = SolidRodFuelCycle(this)

    // ----- mutable physics state -----
    internal var k = 0.0
    override var kEff: Double = 0.0
    internal var controlRodFactor = 0.0
    internal var neutronToPowerConversion = 0.0
    internal var decayNeutrons = 0.0
    internal var neutronPoisonAmount = 0.0
    internal var decayProductsAmount = 0.0
    internal var weightedGenerationTime = 2.0
    var neutronFlux: Double = 0.0
    var power: Double = 0.0
    var temperature: Double = ROOM_TEMPERATURE
    internal var prevTemperature = 0.0
    var pressure: Double = STANDARD_PRESSURE
    override var fuelDepletion: Double = -1.0
    override var accumulatedHydrogen: Double = 0.0

    // ----- coolant-derived -----
    internal var coolantBaseTemperature = 0.0
    internal var coolantBoilingPointStandardPressure = 0.0
    internal var coolantExitTemperature = 0.0
    private var coolantHeatOfVaporization = 0.0
    internal var coolantMass = 0.0
    internal var structuralMass: Double
    var fuelMass: Double = 0.0

    // ----- limits / config -----
    override var maxTemperature: Double = 2000.0
    override var maxPressure: Double = 15000000.0
    override var maxPower: Double = 3.0
    var controlRodInsertion: Double
    var controlRodRegulationOn: Boolean = true
    var isOn: Boolean = false

    init {
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
                when (component) {
                    is FuelRod -> {
                        component.index = fuelIndex++
                        fuelRods.add(component)
                    }

                    is ControlRod -> {
                        component.index = controlIndex++
                        controlRods.add(component)
                    }

                    is CoolantChannel -> {
                        component.index = coolantIndex++
                        component.weight = 0.0
                        coolantChannels.add(component)
                    }
                }
            }
        }
    }

    fun prepareInitialConditions() {
        coolantBaseTemperature = 0.0
        coolantBoilingPointStandardPressure = 0.0
        this.coolantExitTemperature = 0.0
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
                coolantBaseTemperature += original.fluidType.temperature.toDouble()
            }
            coolantBoilingPointStandardPressure += prop.boilingPoint
            coolantExitTemperature += hotCoolant.fluidType.temperature.toDouble()
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

    // ===================== geometry helpers =====================
    // The eigenvalue machinery (computeK, fillGeometricMatrices, runPowerIteration,
    // applyLeakageFactor, assignRodWeightsAndThermalProportions) and the reusable matrices live in
    // LegacyEigenvalueNeutronics. The two helpers below stay here because they mutate this class's
    // own component collections (effectiveControlRods / coolant-channel weights) and are invoked by
    // the kernels during precompute.

    internal fun computeCoolantWeights() {
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

    internal fun computeControlRodWeights(totalWorth: Double) {
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

    // ===================== tick updates =====================

    fun updatePower() {
        // Legacy updatePower body lives in LegacyEigenvalueNeutronics.solve (point kinetics) plus
        // SolidRodFuelCycle.step (fuelDepletion). solve handles both the isOn and !isOn branches;
        // depletion only accrues while running, matching legacy.
        neutronics.solve(state, 1.0)
        if (isOn) {
            fuelCycle.step(state, neutronFlux, reactorDepth.toDouble())
        }
    }

    fun updateTemperature() {
        thermal.updateTemperatureStep()
    }

    fun updatePressure() {
        thermal.updatePressureStep()
    }

    fun updateNeutronPoisoning() {
        this.decayProductsAmount *= decayProductRate
        this.neutronPoisonAmount += this.decayProductsAmount * (1 - decayProductRate) * poisonFraction
        this.neutronPoisonAmount *= decayProductRate * exp(-crossSectionRatio * power / surfaceArea)
    }

    val totalDecayNeutrons: Double
        get() = this.neutronPoisonAmount * 0.05 + this.decayProductsAmount * 0.1 + this.decayNeutrons

    // ===================== ReactorCore API =====================

    override fun precompute() {
        prepareThermalProperties()
        effectiveControlRods.clear()
        if (fuelRods.isEmpty()) {
            k = 0.0
            kEff = 0.0
            maxPower = 0.0
            controlRodFactor = 0.0
            prepareInitialConditions()
            return
        }
        // Legacy computeGeometry decomposed into its neutronics slice (eigenvalue + control-rod
        // worth + neutronToPowerConversion + decayNeutrons + controlRodFactor) and its thermal
        // slice (computeCoolantWeights + maxPower + prepareInitialConditions). Running the
        // neutronics slice first is value-safe: see task-5-report.md parity table.
        neutronics.precompute(geometry)
        thermal.precompute(geometry)
    }

    override fun tick() {
        if (!this.isOn || fuelRods.isEmpty()) return
        updatePower()
        updateTemperature()
        updatePressure()
        updateNeutronPoisoning()
        regulateControlRods()
    }

    override fun save(tag: CompoundTag): CompoundTag {
        val inner = serializeNBT()
        for (key in inner.allKeys) {
            val value = inner.get(key) ?: continue
            tag.put(key, value)
        }
        tag.putString("Family", "supercritical:pwr")
        tag.putInt("Version", 1)
        return tag
    }

    override fun load(tag: CompoundTag) {
        require(tag.getString("Family") == "supercritical:pwr") { "Unknown reactor family: ${tag.getString("Family")}" }
        tag.getInt("Version")
        deserializeNBT(tag)
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
                ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion)
        }
    }

    // ===================== safety =====================
    fun updateControlRodInsertion(controlRodInsertion: Double) {
        this.controlRodInsertion = max(0.001, controlRodInsertion)
        this.controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion)
    }

    override fun turnOff() {
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
     * [serializeNBT]. Should be called after precompute so structural properties are fresh.
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

        internal fun responseFunction(target: Double, current: Double, criticalRate: Double): Double {
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
