package io.github.symmetricdevs.supercritical.api.fission.ecs

import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

/**
 * Typed token for a [io.github.symmetricdevs.supercritical.api.fission.ecs.Component] class.
 *
 * Component types are registered per [ReactorFamily] so that a [io.github.symmetricdevs.supercritical.api.fission.ecs.World] knows how
 * to serialize and query each kind of component. The [id] is used as the NBT key.
 */
class ComponentType<T : io.github.symmetricdevs.supercritical.api.fission.ecs.Component>(
    val id: ResourceLocation,
    val clazz: KClass<T>,
    val serializer: io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentSerializer<T>? = null
) {
    override fun equals(other: Any?): Boolean = other is ComponentType<*> && other.id == id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "ComponentType($id)"
}
