package io.github.symmetricdevs.supercritical.api.nuclear.ecs

/**
 * Ordered list of systems split into [SystemGroup]s.
 */
class Schedule {
    private val groups = linkedMapOf<SystemGroup, MutableList<System>>()

    fun add(group: SystemGroup, system: System): Schedule {
        groups.getOrPut(group) { arrayListOf() }.add(system)
        return this
    }

    fun systems(): List<Pair<SystemGroup, List<System>>> =
        groups.map { it.key to it.value.toList() }

    fun allSystems(): List<System> = groups.values.flatten()

    fun clear() = groups.clear()
}
