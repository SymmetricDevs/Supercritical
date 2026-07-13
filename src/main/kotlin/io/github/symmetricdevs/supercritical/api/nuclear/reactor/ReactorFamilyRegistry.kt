package io.github.symmetricdevs.supercritical.api.nuclear.reactor

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry of reactor-family factories. Addon mods can register custom families
 * before their multiblock controllers are formed; the controller fetches the
 * factory by [ResourceLocation] and passes the reactor size, depth, initial
 * control-rod insertion, and optional saved state.
 */
object ReactorFamilyRegistry {
    private val factories = ConcurrentHashMap<ResourceLocation, (Int, Int, Double, CompoundTag?) -> ReactorCore>()

    /**
     * Registers a reactor family factory. The factory receives the reactor's
     * lattice [size], [depth], initial [controlRodInsertion], and optional NBT
     * [state] (from disk or a saved reactor core), and returns a fully-initialized
     * [ReactorCore].
     */
    fun register(
        id: ResourceLocation,
        factory: (size: Int, depth: Int, controlRodInsertion: Double, state: CompoundTag?) -> ReactorCore
    ) {
        val previous = factories.putIfAbsent(id, factory)
        require(previous == null) { "Reactor family '$id' is already registered" }
    }

    /**
     * Creates a reactor core for the given family [id], or null if no family has
     * been registered under that id.
     */
    operator fun invoke(
        id: ResourceLocation,
        size: Int,
        depth: Int,
        controlRodInsertion: Double,
        state: CompoundTag? = null
    ): ReactorCore? = factories[id]?.let { it(size, depth, controlRodInsertion, state) }
}
