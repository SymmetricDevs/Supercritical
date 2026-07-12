package io.github.symmetricdevs.supercritical.api.nuclear.reactor.pwr

import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.ControlRod
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.NeutronicsKernel
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.NeutronicsResult
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorGeometry
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorState
import io.github.symmetricdevs.supercritical.config.ScritConfig
import net.minecraft.nbt.CompoundTag
import java.util.*
import kotlin.math.exp
import kotlin.math.max

/**
 * Legacy PWR eigenvalue + point-kinetics neutronics, lifted verbatim from the 1.12.2
 * `FissionReactor` physics.
 *
 * This kernel owns the three fuel-rod x fuel-rod geometric matrices ([neutronMatrix], [fastMatrix],
 * [slowMatrix]) that the legacy code reallocated on every `computeK` call. They are sized once in
 * [precompute] and only re-allocated when the rod count changes - the single deliberate behavior
 * change versus the original: allocation, not values.
 *
 * Every field read/write goes through the [reactor] reference, so the legacy mutation order and
 * every computed value (k, kEff, flux, power, decay products, control-rod worth) is preserved
 * byte-for-byte. The [ReactorState] parameter and [NeutronicsResult] return value satisfy the
 * [NeutronicsKernel] contract and carry an informational snapshot; the reactor's own fields remain
 * the authoritative store (legacy was imperative, not pure).
 */
class LegacyEigenvalueNeutronics(private val reactor: FissionReactor) : NeutronicsKernel {

    private var neutronMatrix: Array<DoubleArray> = emptyArray()
    private var fastMatrix: Array<DoubleArray> = emptyArray()
    private var slowMatrix: Array<DoubleArray> = emptyArray()

    override fun precompute(geometry: ReactorGeometry) {
        val fuelRods = reactor.fuelRods
        ensureMatrixCapacity(fuelRods.size)

        // Eigenvalue + control-rod worth: the neutronics slice of legacy computeGeometry. The
        // thermal slice (computeCoolantWeights / calculateMaxPower / prepareInitialConditions) is
        // applied by LegacyPWRThermalHydraulics.precompute after this returns. Reordering the two
        // slices is value-safe: see task-5-report.md parity table.
        reactor.k = computeK(addToEffectiveLists = true, controlRodsInserted = false)
        val kExperimental = computeK(addToEffectiveLists = false, controlRodsInserted = true)
        reactor.computeControlRodWeights(
            ((reactor.k - 1) / reactor.k) - ((kExperimental - 1) / kExperimental)
        )

        reactor.neutronToPowerConversion = 0.0
        reactor.decayNeutrons = 0.0
        for (rod in fuelRods) {
            reactor.neutronToPowerConversion += rod.getFuel().releasedHeatEnergy / rod.getFuel().requiredNeutrons
            reactor.decayNeutrons += rod.getFuel().decayRate
        }

        if (fuelRods.size > 1) {
            reactor.neutronToPowerConversion /= fuelRods.size.toDouble()
        } else {
            reactor.k = 0.00001
        }

        reactor.controlRodFactor =
            ControlRod.controlRodFactor(reactor.effectiveControlRods, reactor.controlRodInsertion)
    }

    /**
     * Legacy point-kinetics update (the body of legacy `updatePower`), minus the `fuelDepletion`
     * accumulation which is owned by [SolidRodFuelCycle]. The depletion line read the same
     * post-evolution `neutronFlux` as the `power` write that remains here, and writes a distinct
     * field, so splitting it off is value-equivalent.
     */
    override fun solve(state: ReactorState, dt: Double): NeutronicsResult {
        if (reactor.isOn) {
            reactor.neutronFlux += reactor.totalDecayNeutrons
            reactor.kEff = 1.0 / ((1.0 / reactor.k) +
                FissionReactor.powerDefectCoefficient * (reactor.power / reactor.maxPower) +
                reactor.neutronPoisonAmount * FissionReactor.crossSectionRatio / reactor.surfaceArea +
                reactor.controlRodFactor)
            reactor.kEff = max(0.0, reactor.kEff)

            val inverseReactorPeriod = (reactor.kEff - 1) / reactor.weightedGenerationTime
            reactor.neutronFlux *= exp(inverseReactorPeriod)

            reactor.decayProductsAmount += max(reactor.neutronFlux, 0.0) / 250000.0
            reactor.power = reactor.neutronFlux * reactor.neutronToPowerConversion
        } else {
            reactor.neutronFlux *= 0.5
            reactor.power *= 0.5
        }
        return NeutronicsResult(
            kEff = reactor.kEff,
            kInf = reactor.k,
            totalFlux = reactor.neutronFlux,
            controlWorth = reactor.controlRodFactor,
            thermalFraction = averageThermalProportion()
        )
    }

    override fun save(tag: CompoundTag) {
        // No kernel-owned persistent state: k, controlRodFactor, neutronToPowerConversion and
        // decayNeutrons are recomputed in precompute(); the geometric matrices are recomputed in
        // computeK(). ReactorCore.save persists the authoritative reactor fields.
    }

    override fun load(tag: CompoundTag) {
        // See save(): nothing to restore.
    }

    // ----- eigenvalue machinery (moved verbatim from FissionReactor) -----

    private fun ensureMatrixCapacity(n: Int) {
        if (neutronMatrix.size == n) return
        neutronMatrix = Array(n) { DoubleArray(n) }
        fastMatrix = Array(n) { DoubleArray(n) }
        slowMatrix = Array(n) { DoubleArray(n) }
    }

    private fun computeK(addToEffectiveLists: Boolean, controlRodsInserted: Boolean): Double {
        fillGeometricMatrices(
            neutronMatrix, fastMatrix, slowMatrix,
            addToEffectiveLists, controlRodsInserted
        )
        val vector = runPowerIteration(neutronMatrix)
        val kCalc = FissionReactor.getMagnitude(vector)
        if (addToEffectiveLists) {
            assignRodWeightsAndThermalProportions(vector, fastMatrix, slowMatrix)
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
        // REUSE CONTRACT — the three matrices are scratch buffers sized once in precompute() and
        // reused across precomputes and across the two computeK() calls within one precompute.
        // Correctness depends on two properties, which MUST hold for every future edit:
        //   1. Every OFF-DIAGONAL cell neutrons[i][j] / neutrons[j][i] (i != j) is written exactly
        //      once below, unconditionally, after the ray-trace loop. runPowerIteration reads every
        //      cell via multiply(), so a skipped write would leak a value from a previous call.
        //   2. The DIAGONAL neutrons[i][i] is NEVER written and must stay 0.0 (the value a fresh
        //      DoubleArray has), so its contribution to multiply() is zero — matching legacy, which
        //      reallocated the matrix every call.
        // fast/slow are only written when addToEffectiveLists is true and only read in the same call
        // (by assignRodWeightsAndThermalProportions); the second computeK call neither writes nor
        // reads them, so their diagonal-stays-0.0 property carries over from initial allocation.
        // If a future change adds a distance/visibility cutoff that could skip an off-diagonal
        // write, zero the matrices here first (Arrays.fill each row) before relying on this.
        val fuelRods = reactor.fuelRods
        val reactorLayout = reactor.reactorLayout
        val resolution = ScritConfig.INSTANCE.nuclear.fissionReactorResolution
        for (i in fuelRods.indices) {
            for (j in 0..<i) {
                var moderation = 0.0
                var slowAbsorption = 0.0
                var fastAbsorption = 0.0
                val rodOne = fuelRods[i]
                val rodTwo = fuelRods[j]

                var prevX = rodOne.x
                var prevY = rodOne.y
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
        val vector = DoubleArray(reactor.fuelRods.size)
        Arrays.fill(vector, 1.0)
        for (i in 0..<ScritConfig.INSTANCE.nuclear.fissionReactorPowerIterations) {
            FissionReactor.normalize(vector)
            FissionReactor.multiply(matrix, vector)
        }
        return vector
    }

    private fun assignRodWeightsAndThermalProportions(
        vector: DoubleArray,
        fastMatrix: Array<DoubleArray>,
        slowMatrix: Array<DoubleArray>
    ) {
        val fuelRods = reactor.fuelRods
        FissionReactor.linearNormalize(vector)
        for (i in fuelRods.indices) {
            fuelRods[i].weight = vector[i]
        }
        val fastVector = vector.copyOf(vector.size)
        val slowVector = vector.copyOf(vector.size)
        FissionReactor.multiply(fastMatrix, fastVector)
        FissionReactor.multiply(slowMatrix, slowVector)
        for (i in fuelRods.indices) {
            if (slowVector[i] + fastVector[i] == 0.0) {
                fuelRods[i].thermalProportion = 0.0
            } else {
                fuelRods[i].thermalProportion = (slowVector[i] / (slowVector[i] + fastVector[i]))
            }
        }
    }

    private fun applyLeakageFactor(kCalc: Double): Double {
        val leakageFactor = reactor.reactorDepth / (1.0 + reactor.reactorDepth)
        return kCalc * leakageFactor
    }

    /**
     * Informational mean of per-rod thermalProportion (set during precompute). Not read back by any
     * legacy formula; populates [NeutronicsResult.thermalFraction] for API consumers.
     */
    private fun averageThermalProportion(): Double {
        val rods = reactor.fuelRods
        if (rods.isEmpty()) return 0.0
        var sum = 0.0
        for (rod in rods) sum += rod.thermalProportion
        return sum / rods.size.toDouble()
    }
}
