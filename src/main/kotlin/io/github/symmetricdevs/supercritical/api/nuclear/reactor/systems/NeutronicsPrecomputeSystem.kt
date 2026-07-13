package io.github.symmetricdevs.supercritical.api.nuclear.reactor.systems

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.System
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.World
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.ControlRodComponent
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.NeutronicsPropertiesComponent
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.PositionComponent
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.ReactorComponentTypes
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.query
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorPhysics
import io.github.symmetricdevs.supercritical.config.ScritConfig
import java.util.Arrays
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Eigenvalue + control-rod-worth precompute for the legacy PWR family.
 * Runs only from [io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorCore.precompute].
 *
 * Reads fuel/control-rod data from the [io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources.ReactorGeometryCache]
 * entity lists and the live component data on those entities. The three geometric
 * matrices are scratch buffers sized to the fuel-rod count and reused across the two
 * `computeK` passes within one precompute — same reuse contract as the legacy kernel:
 * every off-diagonal cell is rewritten each pass, the diagonal stays 0.0.
 */
class NeutronicsPrecomputeSystem : System {

    private var neutronMatrix: Array<DoubleArray> = emptyArray()
    private var fastMatrix: Array<DoubleArray> = emptyArray()
    private var slowMatrix: Array<DoubleArray> = emptyArray()

    // Cell grids rebuilt each precompute (indexed x * size + y, matching the cellEntities grid).
    private var moderation = DoubleArray(0)
    private var absFast = DoubleArray(0)
    private var absSlow = DoubleArray(0)
    private var occupied = BooleanArray(0)
    private var controlRodAt: Array<ControlRodComponent?> = arrayOfNulls(0)

    // Fuel-rod snapshot arrays (stable for the duration of one precompute).
    private var rodX = IntArray(0)
    private var rodY = IntArray(0)
    private var slowFission = DoubleArray(0)
    private var fastFission = DoubleArray(0)
    private var slowCapture = DoubleArray(0)
    private var fastCapture = DoubleArray(0)

    private var size = 0
    private var depth = 0
    private var fuelRodCount = 0

    override fun update(world: World, dt: Double) {
        val cache = world.cache()
        val ng = world.neutronicsGlobals() ?: return
        val lattice = world.lattice() ?: return
        val controlState = world.controlRodState() ?: return
        val fuelRods = cache.fuelRods

        size = lattice.size
        depth = lattice.depth
        fuelRodCount = fuelRods.size

        ensureMatrixCapacity(fuelRodCount)
        buildCellGrids(world)
        snapshotFuelRods(world, fuelRods)

        ng.k = computeK(world, cache, addToEffectiveLists = true, controlRodsInserted = false)
        val kExperimental = computeK(world, cache, addToEffectiveLists = false, controlRodsInserted = true)
        computeControlRodWeights(world, cache, ((ng.k - 1) / ng.k) - ((kExperimental - 1) / kExperimental))

        ng.neutronToPowerConversion = 0.0
        ng.decayNeutrons = 0.0
        for (entity in fuelRods) {
            val fuel = world.getComponent(entity, ReactorComponentTypes.FUEL_ROD)!!.fuel
            ng.neutronToPowerConversion += fuel.releasedHeatEnergy / fuel.requiredNeutrons
            ng.decayNeutrons += fuel.decayRate
        }
        if (fuelRodCount > 1) {
            ng.neutronToPowerConversion /= fuelRodCount.toDouble()
        } else {
            ng.k = 0.00001
        }
        ng.controlRodFactor = controlRodFactor(world, cache, controlState.insertion)
    }

    // ----- cell grids -----

    private fun buildCellGrids(world: World) {
        val n = size * size
        if (moderation.size != n) {
            moderation = DoubleArray(n)
            absFast = DoubleArray(n)
            absSlow = DoubleArray(n)
            occupied = BooleanArray(n)
            controlRodAt = arrayOfNulls(n)
        } else {
            moderation.fill(0.0)
            absFast.fill(0.0)
            absSlow.fill(0.0)
            occupied.fill(false)
            for (i in controlRodAt.indices) controlRodAt[i] = null
        }
        world.query<NeutronicsPropertiesComponent> { entity, neut ->
            val pos = world.getComponent(entity, ReactorComponentTypes.POSITION) ?: return@query
            val idx = pos.x * size + pos.y
            moderation[idx] = neut.moderationFactor
            absFast[idx] = neut.absorptionFast
            absSlow[idx] = neut.absorptionSlow
            occupied[idx] = true
            world.getComponent(entity, ReactorComponentTypes.CONTROL_ROD)?.let { controlRodAt[idx] = it }
        }
    }

    private fun snapshotFuelRods(world: World, fuelRods: List<io.github.symmetricdevs.supercritical.api.nuclear.ecs.Entity>) {
        if (rodX.size != fuelRodCount) {
            rodX = IntArray(fuelRodCount)
            rodY = IntArray(fuelRodCount)
            slowFission = DoubleArray(fuelRodCount)
            fastFission = DoubleArray(fuelRodCount)
            slowCapture = DoubleArray(fuelRodCount)
            fastCapture = DoubleArray(fuelRodCount)
        }
        for (i in fuelRods.indices) {
            val pos = world.getComponent(fuelRods[i], ReactorComponentTypes.POSITION)!!
            val fuel = world.getComponent(fuelRods[i], ReactorComponentTypes.FUEL_ROD)!!.fuel
            rodX[i] = pos.x
            rodY[i] = pos.y
            slowFission[i] = fuel.slowFissionMultiplier
            fastFission[i] = fuel.fastFissionMultiplier
            slowCapture[i] = fuel.slowNeutronCaptureCrossSection
            fastCapture[i] = fuel.fastNeutronCaptureCrossSection
        }
    }

    // ----- eigenvalue machinery (ported verbatim from LegacyEigenvalueNeutronics) -----

    private fun computeK(
        world: World,
        cache: io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources.ReactorGeometryCache,
        addToEffectiveLists: Boolean,
        controlRodsInserted: Boolean
    ): Double {
        fillGeometricMatrices(neutronMatrix, fastMatrix, slowMatrix, addToEffectiveLists, controlRodsInserted)
        val vector = runPowerIteration()
        val kCalc = ReactorPhysics.getMagnitude(vector)
        if (addToEffectiveLists) {
            assignRodWeightsAndThermalProportions(world, cache, vector)
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
        // REUSE CONTRACT — see LegacyEigenvalueNeutronics. The three matrices are scratch
        // buffers reused across both computeK() passes of one precompute. Off-diagonal
        // cells are rewritten every pass; the diagonal is never written and stays 0.0.
        // fast/slow are written only when addToEffectiveLists == true and read in the same
        // pass (by assignRodWeightsAndThermalProportions); the second pass neither writes
        // nor reads them.
        val resolution = ScritConfig.INSTANCE.nuclear.fissionReactorResolution
        val steps = Math.ceil(resolution).toInt()
        for (i in 0 until fuelRodCount) {
            for (j in 0 until i) {
                var moderationSum = 0.0
                var slowAbsorption = 0.0
                var fastAbsorption = 0.0
                val x1 = rodX[i]
                val y1 = rodY[i]
                val x2 = rodX[j]
                val y2 = rodY[j]
                var prevX = x1
                var prevY = y1
                for (t in 0 until steps) {
                    val x = Math.round((x2 - x1) * (t.toDouble() / resolution) + x1).toInt()
                    val y = Math.round((y2 - y1) * (t.toDouble() / resolution) + y1).toInt()
                    if (x < 0 || x > size - 1 || y < 0 || y > size - 1) continue
                    val idx = x * size + y
                    if (!occupied[idx]) continue
                    if (!(x == x1 && y == y1) && !(x == x2 && y == y2)) {
                        val controlOverridden = controlRodsInserted && controlRodAt[idx] != null
                        slowAbsorption += if (controlOverridden) 4.0 else absSlow[idx]
                        fastAbsorption += if (controlOverridden) 4.0 else absFast[idx]
                    }
                    val mod = moderation[idx]
                    if (mod > 0) {
                        moderationSum += mod
                        slowAbsorption = (fastAbsorption + slowAbsorption) / 2
                    }
                    if (!addToEffectiveLists || (x == prevX && y == prevY)) continue
                    prevX = x
                    prevY = y
                    controlRodAt[idx]?.let { it.relatedFuelRodPairs++ }
                }

                moderationSum /= resolution
                fastAbsorption /= resolution
                slowAbsorption /= resolution

                val dist = sqrt((x1 - x2).toDouble().pow(2.0) + (y1 - y2).toDouble().pow(2.0))
                val unabsorbedFast = exp(-fastAbsorption * dist) / dist
                val unabsorbedSlow = exp(-slowAbsorption * dist) / dist
                var fastFlux = exp(-moderationSum * dist) / dist
                val slowFlux = (1 / dist - fastFlux) * unabsorbedSlow
                fastFlux *= unabsorbedFast

                neutrons[i][j] = slowFlux * slowFission[j] + fastFlux * fastFission[j]
                neutrons[j][i] = slowFlux * slowFission[i] + fastFlux * fastFission[i]

                if (addToEffectiveLists) {
                    fast[i][j] = fastFlux * fastCapture[j]
                    slow[i][j] = slowFlux * slowCapture[j]
                    fast[j][i] = fastFlux * fastCapture[i]
                    slow[j][i] = slowFlux * slowCapture[i]
                }
            }
        }
    }

    private fun runPowerIteration(): DoubleArray {
        val vector = DoubleArray(fuelRodCount)
        Arrays.fill(vector, 1.0)
        for (i in 0 until ScritConfig.INSTANCE.nuclear.fissionReactorPowerIterations) {
            ReactorPhysics.normalize(vector)
            ReactorPhysics.multiply(neutronMatrix, vector)
        }
        return vector
    }

    private fun assignRodWeightsAndThermalProportions(
        world: World,
        cache: io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources.ReactorGeometryCache,
        vector: DoubleArray
    ) {
        val fuelRods = cache.fuelRods
        ReactorPhysics.linearNormalize(vector)
        for (i in fuelRods.indices) {
            world.getComponent(fuelRods[i], ReactorComponentTypes.FUEL_ROD)!!.weight = vector[i]
        }
        val fastVector = vector.copyOf(vector.size)
        val slowVector = vector.copyOf(vector.size)
        ReactorPhysics.multiply(fastMatrix, fastVector)
        ReactorPhysics.multiply(slowMatrix, slowVector)
        for (i in fuelRods.indices) {
            val rod = world.getComponent(fuelRods[i], ReactorComponentTypes.FUEL_ROD)!!
            rod.thermalProportion = if (slowVector[i] + fastVector[i] == 0.0) {
                0.0
            } else {
                slowVector[i] / (slowVector[i] + fastVector[i])
            }
        }
    }

    private fun applyLeakageFactor(kCalc: Double): Double = kCalc * depth / (1.0 + depth)

    // ----- control-rod worth (ported from FissionReactor.computeControlRodWeights + ControlRod) -----

    private fun computeControlRodWeights(
        world: World,
        cache: io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources.ReactorGeometryCache,
        totalWorth: Double
    ) {
        var totalWeight = 0.0
        for (entity in cache.controlRods) {
            val rod = world.getComponent(entity, ReactorComponentTypes.CONTROL_ROD)!!
            rod.weight = (rod.relatedFuelRodPairs * 4).toDouble()
            if (rod.weight > 0) {
                cache.effectiveControlRods.add(entity)
                totalWeight += rod.weight
            }
        }
        if (totalWeight != 0.0) {
            for (entity in cache.effectiveControlRods) {
                val rod = world.getComponent(entity, ReactorComponentTypes.CONTROL_ROD)!!
                rod.weight = rod.weight / totalWeight * totalWorth
            }
        }
    }

    private fun ensureMatrixCapacity(n: Int) {
        if (neutronMatrix.size == n) return
        neutronMatrix = Array(n) { DoubleArray(n) }
        fastMatrix = Array(n) { DoubleArray(n) }
        slowMatrix = Array(n) { DoubleArray(n) }
    }
}
