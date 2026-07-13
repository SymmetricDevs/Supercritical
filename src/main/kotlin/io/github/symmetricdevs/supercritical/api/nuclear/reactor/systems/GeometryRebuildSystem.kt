package io.github.symmetricdevs.supercritical.api.nuclear.reactor.systems

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Entity
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.System
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.World
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.*
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.ReactorComponentTypes
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.query
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources.ReactorGeometryCache
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources.RootEntityResource
import kotlin.math.min

// ----------------------------------------------------------------------------------
// Root-entity / global-component accessors shared by every reactor ECS system.
// The root entity is published as RootEntityResource by the ReactorCore facade.
// ----------------------------------------------------------------------------------

/** Returns the reactor root entity, or null if no root has been published. */
fun World.rootEntity(): Entity? = resources.get<RootEntityResource>()?.entity

/** Returns the global reactor-state component on the root entity, or null. */
fun World.state(): ReactorStateComponent? {
    val root = rootEntity() ?: return null
    return getComponent(root, ReactorComponentTypes.REACTOR_STATE)
}

/** Returns the global neutronics-globals component on the root entity, or null. */
fun World.neutronicsGlobals(): NeutronicsGlobalsComponent? {
    val root = rootEntity() ?: return null
    return getComponent(root, ReactorComponentTypes.NEUTRONICS_GLOBALS)
}

/** Returns the global thermal-globals component on the root entity, or null. */
fun World.thermalGlobals(): ThermalGlobalsComponent? {
    val root = rootEntity() ?: return null
    return getComponent(root, ReactorComponentTypes.THERMAL_GLOBALS)
}

/** Returns the global reactor-limits component on the root entity, or null. */
fun World.limits(): ReactorLimitsComponent? {
    val root = rootEntity() ?: return null
    return getComponent(root, ReactorComponentTypes.REACTOR_LIMITS)
}

/** Returns the global control-rod-state component on the root entity, or null. */
fun World.controlRodState(): ControlRodStateComponent? {
    val root = rootEntity() ?: return null
    return getComponent(root, ReactorComponentTypes.CONTROL_ROD_STATE)
}

/** Returns the lattice-geometry component on the root entity, or null. */
fun World.lattice(): LatticeGeometryComponent? {
    val root = rootEntity() ?: return null
    return getComponent(root, ReactorComponentTypes.LATTICE_GEOMETRY)
}

/** Returns the cached reactor geometry, creating it on first access. */
fun World.cache(): ReactorGeometryCache = resources.getOrCreate { ReactorGeometryCache() }

// ----------------------------------------------------------------------------------
// GeometryRebuildSystem — rebuilds the reactor geometry cache and thermal aggregates.
//
// Runs only from ReactorCore.precompute() (NOT from the per-tick schedule): it resets
// coolantMass / maxTemperature, which would clobber the thermal system's per-tick
// updates if it ran every tick. It collects fuel/control/coolant cell entities into
// the geometry cache in stable row-major order and recomputes the structural mass,
// surface area, and max-temperature aggregate exactly as the legacy code did.
// ----------------------------------------------------------------------------------

/**
 * Rebuilds the [ReactorGeometryCache] entity lists and the global thermal/limit
 * aggregates from the current lattice components.
 *
 * Mirrors legacy `prepareThermalProperties`: every cell's mass is folded into
 * [ThermalGlobalsComponent.structuralMass] (fuel/coolant masses are NOT separated
 * here — `fuelMass` and `coolantMass` start at 0, matching legacy; `coolantMass`
 * is repopulated each tick by the thermal system), and [ReactorLimitsComponent.maxTemperature]
 * is the min of every cell's max temperature (clamped to a 2000 K ceiling).
 */
class GeometryRebuildSystem : System {
    override fun update(world: World, dt: Double) {
        val cache = world.cache()
        cache.clear()
        val thermal = world.thermalGlobals() ?: return
        val limits = world.limits() ?: return
        val lattice = world.lattice() ?: return
        val size = lattice.size
        val depth = lattice.depth
        val radius = size / 2.0 + 1.5

        thermal.surfaceArea = radius * radius * Math.PI * 2 + depth * radius * Math.PI * 2
        thermal.structuralMass = depth * radius * radius * Math.PI * 300
        thermal.fuelMass = 0.0
        thermal.coolantMass = 0.0
        limits.maxTemperature = 2000.0

        // Fold every cell's mass + max-temperature into the structural aggregate.
        world.query<ThermalPropertiesComponent> { _, therm ->
            limits.maxTemperature = min(limits.maxTemperature, therm.maxTemperature)
            thermal.structuralMass += therm.mass
        }

        // Collect the typed cell lists in row-major (x, then y) order — the same order the cell
        // entities are created by setFuelRod/setControlRod/..., so eigenvalue matrix indices and
        // weight assignment stay stable.
        populateRowMajor<FuelRodComponent>(world, cache.fuelRods)
        populateRowMajor<ControlRodComponent>(world, cache.controlRods)
        populateRowMajor<CoolantChannelComponent>(world, cache.coolantChannels)
    }
}

/**
 * Appends every cell carrying [P] to [out], sorted by lattice position (x, then y).
 * Reads [PositionComponent] per entity (every reactor cell has one, set in setComponent).
 */
private inline fun <reified P : Component> populateRowMajor(world: World, out: ArrayList<Entity>) {
    val positioned = ArrayList<Pair<Entity, PositionComponent>>()
    world.query<P> { entity, _ ->
        val pos = world.getComponent(entity, ReactorComponentTypes.POSITION) ?: return@query
        positioned.add(entity to pos)
    }
    positioned.sortWith(compareBy({ it.second.x }, { it.second.y }))
    for ((entity, _) in positioned) {
        out.add(entity)
    }
}
