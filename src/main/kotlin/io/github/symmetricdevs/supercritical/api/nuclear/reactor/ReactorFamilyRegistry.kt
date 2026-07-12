package io.github.symmetricdevs.supercritical.api.nuclear.reactor

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry of reactor-family factories. Addon mods can register custom families
 * before their multiblock controllers are formed; the controller fetches the
 * factory by [ResourceLocation] and passes the family-specific [ReactorGeometry]
 * and optional saved state.
 */
object ReactorFamilyRegistry {
    private val factories = ConcurrentHashMap<ResourceLocation, (ReactorGeometry, CompoundTag?) -> ReactorCore>()

    /**
     * Registers a reactor family factory. The factory receives the reactor's
     * geometry and optional NBT state (from disk or a saved reactor core) and
     * returns a fully-initialized [ReactorCore].
     */
    fun register(
        id: ResourceLocation,
        factory: (ReactorGeometry, CompoundTag?) -> ReactorCore
    ) {
        val previous = factories.putIfAbsent(id, factory)
        require(previous == null) { "Reactor family '$id' is already registered" }
    }

    /**
     * Returns the factory for the given family [id], or null if no family has
     * been registered under that id.
     */
    operator fun get(id: ResourceLocation): ((ReactorGeometry, CompoundTag?) -> ReactorCore)? =
        factories[id]
}
