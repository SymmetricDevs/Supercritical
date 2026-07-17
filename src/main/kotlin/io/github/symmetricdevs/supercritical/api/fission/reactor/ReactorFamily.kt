package io.github.symmetricdevs.supercritical.api.fission.reactor

import io.github.symmetricdevs.supercritical.api.fission.ecs.Schedule
import io.github.symmetricdevs.supercritical.api.fission.ecs.registration.ComponentTypeRegistry
import net.minecraft.resources.ResourceLocation

/**
 * Family discriminator. Open, not sealed, so addon mods can register new families.
 *
 * A family defines its own component set and system schedule; the multiblock
 * controller only knows the family id and asks [ReactorFamilyRegistry] to build
 * the concrete [ReactorCore].
 */
interface ReactorFamily {
    val id: ResourceLocation

    /**
     * Registers any component types this family needs beyond the built-in ones.
     * Called once during mod initialization.
     */
    fun registerComponents(registry: ComponentTypeRegistry) {}

    /**
     * Builds the fixed execution schedule for this family's simulation.
     */
    fun buildSchedule(): Schedule = Schedule()
}
