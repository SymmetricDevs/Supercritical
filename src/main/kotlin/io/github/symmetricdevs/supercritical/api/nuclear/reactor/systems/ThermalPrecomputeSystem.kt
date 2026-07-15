package io.github.symmetricdevs.supercritical.api.nuclear.reactor.systems

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.System
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.World
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.CoolantChannelComponent
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.PositionComponent
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.ReactorComponentTypes
import io.github.symmetricdevs.supercritical.api.nuclear.fission.CoolantRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorPhysics
import io.github.symmetricdevs.supercritical.config.ScritConfig
import kotlin.math.min

/**
 * Thermal slice of the legacy PWR precompute, ported from `LegacyPWRThermalHydraulics.precompute`.
 * Runs only from [io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorCore.precompute]
 * (after [NeutronicsPrecomputeSystem], which assigns fuel-rod weights).
 *
 * Three steps, in legacy order:
 *  1. [computeCoolantWeights] — distribute each fuel rod's weight to its 4 neighboring
 *     coolant channels (legacy `computeCoolantWeights`).
 *  2. derive `maxPower` — `calculateMaxPower` for >1 fuel rod, else `0.1 * nuclearPowerMultiplier`.
 *  3. [prepareInitialConditions] — average coolant inlet/exit/boiling/heat-of-vaporization
 *     temperatures and the weighted neutron generation time; flip the reactor on.
 *
 * [prepareInitialConditionsOnly] exposes step 3 alone for the no-fuel precompute branch.
 */
class ThermalPrecomputeSystem : System {

    override fun update(world: World, dt: Double) {
        val cache = world.cache()
        computeCoolantWeights(world)
        val maxPower = if (cache.fuelRods.size > 1) {
            calculateMaxPower(world)
        } else {
            0.1 * ScritConfig.INSTANCE.nuclear.nuclearPowerMultiplier
        }
        world.limits()?.maxPower = maxPower
        prepareInitialConditions(world)
    }

    /** Runs only [prepareInitialConditions]; used by the empty-lattice precompute branch. */
    fun prepareInitialConditionsOnly(world: World) {
        prepareInitialConditions(world)
    }

    private fun computeCoolantWeights(world: World) {
        val cache = world.cache()
        val lattice = world.lattice() ?: return
        val size = lattice.size
        val coolantAt = HashMap<Int, CoolantChannelComponent>()
        for (entity in cache.coolantChannels) {
            val pos = world.getComponent(entity, ReactorComponentTypes.POSITION) ?: continue
            val channel = world.getComponent(entity, ReactorComponentTypes.COOLANT_CHANNEL) ?: continue
            coolantAt[pos.x * size + pos.y] = channel
        }
        val dx = intArrayOf(0, 1, 0, -1)
        val dy = intArrayOf(1, 0, -1, 0)
        for (fuelEntity in cache.fuelRods) {
            val pos = world.getComponent(fuelEntity, ReactorComponentTypes.POSITION) ?: continue
            val weight = world.getComponent(fuelEntity, ReactorComponentTypes.FUEL_ROD)?.weight ?: continue
            for (i in 0..3) {
                val nx = pos.x + dx[i]
                val ny = pos.y + dy[i]
                if (nx !in 0..<size || ny < 0 || ny >= size) continue
                coolantAt[nx * size + ny]?.let { it.weight += weight }
            }
        }
    }

    private fun calculateMaxPower(world: World): Double {
        val cache = world.cache()
        val thermal = world.thermalGlobals() ?: return 0.0
        val limits = world.limits() ?: return 0.0
        val lattice = world.lattice() ?: return 0.0
        val hypotheticalTemperature = min(limits.maxTemperature, ReactorPhysics.zircaloyHydrogenReactionTemperature)
        var heatRemoved = 0.0
        for (entity in cache.coolantChannels) {
            val prop = world.getComponent(entity, ReactorComponentTypes.COOLANT_CHANNEL)?.coolant ?: continue
            val coolantTemp = coolantInletTemp(prop)
            val hotCoolant = requireNotNull(prop.hotCoolant) {
                "Coolant must define a hot coolant fluid for maximum-power calculation"
            }
            val cooledTemperature = hotCoolant.fluidType.temperature
            if (cooledTemperature > hypotheticalTemperature) continue
            val heatRemovedPerLiter = heatRemovedPerLiter(prop, coolantTemp, cooledTemperature)
            val idealFluidUsed =
                idealCoolantHeatFlux(prop, world.getComponent(entity, ReactorComponentTypes.COOLANT_CHANNEL)!!.weight, lattice.depth, hypotheticalTemperature, cooledTemperature) /
                    heatRemovedPerLiter
            heatRemoved += idealFluidUsed * heatRemovedPerLiter
        }
        val timeConstant = thermalTimeConstant(thermal.surfaceArea)
        return ((hypotheticalTemperature - thermal.envTemperature) *
            (timeConstant * (thermal.coolantMass + thermal.structuralMass + thermal.fuelMass)) + heatRemoved) / 1e6
    }

    private fun prepareInitialConditions(world: World) {
        val cache = world.cache()
        val thermal = world.thermalGlobals() ?: return
        val ng = world.neutronicsGlobals() ?: return
        val state = world.state() ?: return

        thermal.coolantBaseTemperature = 0.0
        thermal.coolantBoilingPointStandardPressure = 0.0
        thermal.coolantExitTemperature = 0.0
        thermal.coolantHeatOfVaporization = 0.0
        ng.weightedGenerationTime = 0.0

        for (entity in cache.fuelRods) {
            val fuel = world.getComponent(entity, ReactorComponentTypes.FUEL_ROD)?.fuel ?: continue
            ng.weightedGenerationTime += fuel.neutronGenerationTime
        }
        ng.weightedGenerationTime = if (cache.fuelRods.isEmpty()) {
            2.0
        } else {
            ng.weightedGenerationTime / cache.fuelRods.size.toDouble()
        }

        for (entity in cache.coolantChannels) {
            val prop = world.getComponent(entity, ReactorComponentTypes.COOLANT_CHANNEL)?.coolant ?: continue
            val original = CoolantRegistry.originalFluid(prop)
            val hotCoolant = requireNotNull(prop.hotCoolant) { "Coolant must define a hot coolant fluid" }
            if (original != null) {
                thermal.coolantBaseTemperature += original.fluidType.temperature.toDouble()
            }
            thermal.coolantBoilingPointStandardPressure += prop.boilingPoint
            thermal.coolantExitTemperature += hotCoolant.fluidType.temperature.toDouble()
            thermal.coolantHeatOfVaporization += prop.heatOfVaporization
        }

        if (cache.coolantChannels.isNotEmpty()) {
            val count = cache.coolantChannels.size.toDouble()
            thermal.coolantBaseTemperature /= count
            thermal.coolantBoilingPointStandardPressure /= count
            thermal.coolantExitTemperature /= count
            thermal.coolantHeatOfVaporization /= count
            if (thermal.coolantBaseTemperature == 0.0) {
                thermal.coolantBaseTemperature = thermal.envTemperature
            }
            if (thermal.coolantBoilingPointStandardPressure == 0.0) {
                thermal.coolantBoilingPointStandardPressure = ReactorPhysics.AIR_BOILING_POINT
            }
        }
        state.isOn = true
    }
}
