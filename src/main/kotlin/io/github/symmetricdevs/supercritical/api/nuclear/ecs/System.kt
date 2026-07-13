package io.github.symmetricdevs.supercritical.api.nuclear.ecs

/**
 * A system that operates on component data in a [World].
 *
 * Systems are executed in the order defined by the world's [Schedule]. They may
 * read and write components, query entities, emit events, and enqueue commands.
 * They must not directly mutate the world outside of [Commands].
 */
interface System {
    /** Called once per tick with the simulation time step in seconds. */
    fun update(world: World, dt: Double)
}

/**
 * Logical group of systems. The schedule orders groups; all systems in a group
 * run in registration order.
 */
enum class SystemGroup {
    PRE_TICK,
    NEUTRONICS,
    FUEL_CYCLE,
    FUEL_HANDLING,
    THERMAL_HYDRAULICS,
    CONTROL
}
