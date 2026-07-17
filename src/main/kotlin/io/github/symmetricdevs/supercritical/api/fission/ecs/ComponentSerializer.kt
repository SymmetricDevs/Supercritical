package io.github.symmetricdevs.supercritical.api.fission.ecs

import net.minecraft.nbt.CompoundTag
import org.jetbrains.annotations.ApiStatus

/**
 * Serializer pair for a component type.
 *
 * **⚠️ EXPERIMENTAL — NOT YET IMPLEMENTED.**
 * No component type currently has a serializer attached ([ComponentType.serializer] is
 * always `null`). The planned use is generic world-save (iterate root-entity components,
 * delegate to each type's serializer), but this infrastructure is not wired in yet.
 * Until it is, persistence is handled manually in [PWRCore.serializeNBT].
 *
 * For transient components leave [serializer] null.
 */
interface ComponentSerializer<T : Component> {
    fun save(component: T, tag: CompoundTag)
    fun load(tag: CompoundTag): T
}
