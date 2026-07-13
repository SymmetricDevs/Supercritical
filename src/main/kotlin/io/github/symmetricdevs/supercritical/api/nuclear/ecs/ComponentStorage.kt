package io.github.symmetricdevs.supercritical.api.nuclear.ecs

import java.util.IdentityHashMap

/**
 * Column storage for one component type.
 *
 * Each [ComponentStorage] is a dense array indexed by entity index. Unused slots
 * hold null. Dense storage is appropriate because reactor lattices are compact:
 * every cell is an entity, and the root entity sits at index 0.
 */
class ComponentStorage<T : Component>(val type: ComponentType<T>) {

    private val data: ArrayList<T?> = arrayListOf()

    operator fun get(index: Int): T? {
        return if (index in 0 until data.size) data[index] else null
    }

    operator fun set(index: Int, value: T?) {
        ensureCapacity(index + 1)
        data[index] = value
    }

    fun remove(index: Int) {
        if (index in 0 until data.size) {
            data[index] = null
        }
    }

    fun has(index: Int): Boolean =
        index in 0 until data.size && data[index] != null

    private fun ensureCapacity(capacity: Int) {
        while (data.size < capacity) {
            data.add(null)
        }
    }

    companion object {
        /** Lookup from component type to storage inside a [World]. */
        fun createMap(): MutableMap<ComponentType<*>, ComponentStorage<*>> =
            IdentityHashMap()
    }
}
