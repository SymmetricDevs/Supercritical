package io.github.symmetricdevs.supercritical.api.nuclear.ecs

import kotlin.reflect.KClass

/**
 * Container for [Resource] instances stored in a [World].
 */
class Resources {
    private val resources = mutableMapOf<KClass<out Resource>, Resource>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Resource> get(type: KClass<T>): T? = resources[type] as? T

    fun <T : Resource> getOrCreate(type: KClass<T>, factory: () -> T): T {
        var existing = get(type)
        if (existing == null) {
            existing = factory()
            resources[type] = existing
        }
        return existing
    }

    inline fun <reified T : Resource> get(): T? = get(T::class)

    inline fun <reified T : Resource> getOrCreate(noinline factory: () -> T): T =
        getOrCreate(T::class, factory)

    fun <T : Resource> set(type: KClass<T>, resource: T) {
        resources[type] = resource
    }

    inline fun <reified T : Resource> set(resource: T) = set(T::class, resource)

    fun clear() = resources.clear()
}
