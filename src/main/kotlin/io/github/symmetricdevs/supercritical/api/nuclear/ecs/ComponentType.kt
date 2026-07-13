package io.github.symmetricdevs.supercritical.api.nuclear.ecs

import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

/**
 * Typed token for a [Component] class.
 *
 * Component types are registered per [ReactorFamily] so that a [World] knows how
 * to serialize and query each kind of component. The [id] is used as the NBT key.
 */
class ComponentType<T : Component>(
    val id: ResourceLocation,
    val clazz: KClass<T>,
    val serializer: ComponentSerializer<T>? = null
) {
    override fun equals(other: Any?): Boolean = other is ComponentType<*> && other.id == id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "ComponentType($id)"
}
