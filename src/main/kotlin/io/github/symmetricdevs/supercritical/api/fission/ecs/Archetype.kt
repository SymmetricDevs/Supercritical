package io.github.symmetricdevs.supercritical.api.fission.ecs

/**
 * Fingerprint of the set of component types attached to an entity.
 *
 * Archetypes are interned in a [io.github.symmetricdevs.supercritical.api.fission.ecs.World] so that entities with the same component
 * composition share one archetype instance. This makes archetype-based queries
 * fast: a query can precompute which archetypes match and iterate only matching
 * entities.
 */
class Archetype private constructor(
    val id: Int,
    componentTypes: List<io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType<*>>
) {
    private val typeSet: Set<io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType<*>> = componentTypes.toSet()
    private val typeList: List<io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType<*>> = componentTypes.toList()

    val components: List<io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType<*>> get() = typeList

    fun contains(type: io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType<*>): Boolean = typeSet.contains(type)

    fun containsAny(types: Collection<io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType<*>>): Boolean =
        types.any { contains(it) }

    fun containsAll(types: Collection<io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType<*>>): Boolean =
        typeSet.containsAll(types)

    override fun equals(other: Any?): Boolean = other is Archetype && other.typeSet == typeSet

    override fun hashCode(): Int = typeSet.hashCode()

    override fun toString(): String = "Archetype(id=$id, components=${typeList.map { it.id }})"

    companion object {
        const val EMPTY_ID = -1
        private var nextId = 0
        private val cache = mutableMapOf<Set<io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType<*>>, Archetype>()

        /** The archetype with no components. */
        val EMPTY: Archetype = of(emptySet())

        /** Returns the canonical archetype for the given set of component types. */
        fun of(componentTypes: Set<io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType<*>>): Archetype =
            cache.getOrPut(componentTypes) {
                Archetype(nextId++, componentTypes.sortedBy { it.id.toString() })
            }
    }
}
