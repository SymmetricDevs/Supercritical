package io.github.symmetricdevs.supercritical.api.fission.ecs.registration

import io.github.symmetricdevs.supercritical.api.fission.ecs.Component
import io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Global registry of [ComponentType] tokens.
 *
 * Supercritical registers its built-in reactor component types during init; addon
 * mods can register additional component types through [ReactorAddonEntrypoint].
 *
 * The registry is keyed two ways:
 * - by [ComponentType.id] (the NBT-stable [ResourceLocation]), and
 * - by [ComponentType.clazz] (the [KClass] of the component), so that reified
 *   query factories (see [io.github.symmetricdevs.supercritical.api.fission.ecs.query])
 *   can resolve a [ComponentType] from a generic type parameter without a runtime
 *   token argument.
 *
 * Assumption: each registered [Component] class maps to exactly one [ComponentType].
 * The built-in reactor family registers every type through
 * [io.github.symmetricdevs.supercritical.api.fission.ecs.components.ReactorComponentTypes.registerAll]
 * during addon init, before any [io.github.symmetricdevs.supercritical.api.fission.ecs.World]
 * ticks. This is a single global registry shared by all worlds and families, which
 * is acceptable for the current single-family PWR design.
 */
object ComponentTypeRegistry {
    private val types = ConcurrentHashMap<ResourceLocation, ComponentType<*>>()
    private val byClassMap = ConcurrentHashMap<KClass<out Component>, ComponentType<*>>()

    /**
     * Registers a component type. The [ComponentType.id] and [ComponentType.clazz]
     * must each be unique; a conflicting registration throws [IllegalArgumentException].
     *
     * Re-registering the exact same [ComponentType] instance is a no-op, so families may
     * self-register from their construction path even when addon init has already registered
     * the built-in types.
     */
    fun <T : Component> register(type: ComponentType<T>) {
        val previousById = types.putIfAbsent(type.id, type)
        require(previousById == null || previousById === type) {
            "Component type '${type.id}' is already registered"
        }
        val previousByClass = byClassMap.putIfAbsent(type.clazz, type)
        require(previousByClass == null || previousByClass === type) {
            "Component class '${type.clazz.qualifiedName}' is already registered to type '${previousByClass?.id}'"
        }
    }

    /** Returns the registered component type for [id], or null if none is registered. */
    operator fun get(id: ResourceLocation): ComponentType<*>? = types[id]

    /** Returns the registered component type for [clazz], or null if none is registered. */
    fun byClass(clazz: KClass<out Component>): ComponentType<*>? = byClassMap[clazz]

    /** Returns all registered component types. */
    fun all(): Collection<ComponentType<*>> = types.values

    /**
     * Resolves the registered [ComponentType] for the reified component class [T].
     *
     * Used by the reified [io.github.symmetricdevs.supercritical.api.fission.ecs.query]
     * factories so call sites can write `world.query<FuelRodComponent> { entity, rod -> ... }` without
     * passing a runtime [ComponentType] token. Throws [IllegalArgumentException] if
     * [T] was never registered (e.g. because [registerAll] has not run yet).
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Component> componentType(): ComponentType<T> {
        val raw = byClass(T::class)
        requireNotNull(raw) {
            "No ComponentType is registered for ${T::class.qualifiedName}. " +
                "Ensure ReactorComponentTypes.registerAll(ComponentTypeRegistry) runs " +
                "before the world ticks."
        }
        return raw as ComponentType<T>
    }
}
