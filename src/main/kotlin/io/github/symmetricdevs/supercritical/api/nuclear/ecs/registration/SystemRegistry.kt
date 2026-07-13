package io.github.symmetricdevs.supercritical.api.nuclear.ecs.registration

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.System
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.ConcurrentHashMap

/**
 * Global registry of named system factories.
 *
 * Addon mods can register reusable systems here and reference them when building
 * their [io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorFamily]
 * schedules.
 */
object SystemRegistry {
    private val systems = ConcurrentHashMap<ResourceLocation, () -> System>()

    /**
     * Registers a system factory under [id]. A duplicate registration throws
     * [IllegalArgumentException].
     */
    fun register(id: ResourceLocation, factory: () -> System) {
        val previous = systems.putIfAbsent(id, factory)
        require(previous == null) { "System '$id' is already registered" }
    }

    /** Creates an instance of the system registered under [id], or null. */
    fun create(id: ResourceLocation): System? = systems[id]?.invoke()

    /** Returns all registered system ids. */
    fun ids(): Set<ResourceLocation> = systems.keys
}
