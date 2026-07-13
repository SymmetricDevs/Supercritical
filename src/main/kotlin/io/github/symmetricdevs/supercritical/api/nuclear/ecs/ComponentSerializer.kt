package io.github.symmetricdevs.supercritical.api.nuclear.ecs

import net.minecraft.nbt.CompoundTag

/**
 * Serializer pair for a component type.
 *
 * Not every component needs to be persisted (e.g. [PositionComponent] can be
 * reconstructed from the lattice). For transient components leave [serializer] null.
 */
interface ComponentSerializer<T : Component> {
    fun save(component: T, tag: CompoundTag)
    fun load(tag: CompoundTag): T
}
