package io.github.symmetricdevs.supercritical.api.nuclear.ecs

import java.util.BitSet

/**
 * Fingerprint of the set of component types attached to an entity.
 *
 * Archetypes are interned in a [World] so that entities with the same component
 * composition share one archetype instance. This makes archetype-based queries
 * fast: a query can precompute which archetypes match and iterate only matching
 * entities.
 */
class Archetype private constructor(
    val id: Int,
    componentTypes: List<ComponentType<*>>
) {
    private val typeSet: Set<ComponentType<*>> = componentTypes.toSet()
    private val typeList: List<ComponentType<*>> = componentTypes.toList()

    val components: List<ComponentType<*>> get() = typeList

    fun contains(type: ComponentType<*>): Boolean = typeSet.contains(type)

    fun containsAny(types: Collection<ComponentType<*>>): Boolean =
        types.any { contains(it) }

    fun containsAll(types: Collection<ComponentType<*>>): Boolean =
        typeSet.containsAll(types)

    override fun equals(other: Any?): Boolean = other is Archetype && other.typeSet == typeSet

    override fun hashCode(): Int = typeSet.hashCode()

    override fun toString(): String = "Archetype(id=$id, components=${typeList.map { it.id }})"

    companion object {
        const val EMPTY_ID = -1
        private var nextId = 0
        private val cache = mutableMapOf<Set<ComponentType<*>>, Archetype>()

        /** The archetype with no components. */
        val EMPTY: Archetype = of(emptySet())

        /** Returns the canonical archetype for the given set of component types. */
        fun of(componentTypes: Set<ComponentType<*>>): Archetype =
            cache.getOrPut(componentTypes) {
                Archetype(nextId++, componentTypes.sortedBy { it.id.toString() })
            }
    }
}
