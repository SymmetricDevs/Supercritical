package io.github.symmetricdevs.supercritical.api.nuclear.ecs

import kotlin.reflect.KClass

/**
 * The ECS world. Owns entities, component storage, resources, and the schedule.
 *
 * A [ReactorCore] owns exactly one world. The world is ticked once per second
 * by calling [update].
 */
class World(val schedule: Schedule) {

    private val storages = ComponentStorage.createMap()
    private val archetypes = mutableMapOf<Int, Archetype>()
    private val entitySlots = ArrayList<EntitySlot>()
    private val entityArchetypes = IntArrayList()
    private val freeSlots = ArrayDeque<Int>()
    private var nextEntityId = 0

    val resources = Resources()
    val commands = Commands(this)
    val events = mutableMapOf<KClass<out ReactorEvent>, EventQueue<*>>()

    /** Returns the event queue for [E], creating it if necessary. */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified E : ReactorEvent> eventQueue(): EventQueue<E> =
        events.getOrPut(E::class) { EventQueue<E>() } as EventQueue<E>

    /** Creates a new entity with no components. */
    fun createEntity(): Entity {
        val index = if (freeSlots.isNotEmpty()) {
            freeSlots.removeFirst()
        } else {
            entitySlots.add(EntitySlot(nextEntityId))
            entityArchetypes.add(Archetype.EMPTY_ID)
            nextEntityId++
            entitySlots.size - 1
        }
        val slot = entitySlots[index]
        slot.generation = (slot.generation + 1).toByte()
        slot.isOccupied = true
        entityArchetypes[index] = Archetype.EMPTY.id
        return Entity(index, slot.generation.toInt())
    }

    /** Returns true if the entity reference is still valid. */
    fun isAlive(entity: Entity): Boolean {
        if (entity.index !in entitySlots.indices) return false
        val slot = entitySlots[entity.index]
        return slot.isOccupied && slot.generation == entity.generation.toByte()
    }

    /**
     * Adds or replaces a component on an entity. This may change the entity's
     * archetype. Prefer [addComponent] with an explicit [ComponentType].
     */
    fun addComponent(entity: Entity, component: Component) {
        val type = componentTypeOf(component)
        @Suppress("UNCHECKED_CAST")
        addComponent(entity, type as ComponentType<Component>, component)
    }

    /** Removes a component from an entity. */
    fun removeComponent(entity: Entity, type: ComponentType<*>) {
        require(isAlive(entity)) { "Cannot remove component from dead entity $entity" }
        val index = entity.index
        storage(type).remove(index)
        rebuildArchetype(index)
    }

    /** Returns the component of the given type attached to [entity], or null. */
    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponent(entity: Entity, type: ComponentType<T>): T? {
        if (!isAlive(entity)) return null
        return storage(type)[entity.index] as? T
    }

    /** Returns true if [entity] has a component of the given type. */
    fun hasComponent(entity: Entity, type: ComponentType<*>): Boolean {
        if (!isAlive(entity)) return false
        return storage(type).has(entity.index)
    }

    /** Destroys an entity and removes all its components. */
    fun destroyEntity(entity: Entity) {
        require(isAlive(entity)) { "Cannot destroy dead entity $entity" }
        val index = entity.index
        val archetypeId = entityArchetypes[index]
        val archetype = if (archetypeId == Archetype.EMPTY.id) Archetype.EMPTY else archetypes[archetypeId]
        if (archetype != null) {
            for (type in archetype.components) {
                storage(type).remove(index)
            }
        }
        entityArchetypes[index] = Archetype.EMPTY.id
        val slot = entitySlots[index]
        slot.isOccupied = false
        slot.generation = (slot.generation + 1).toByte()
        freeSlots.addLast(index)
    }

    /** Runs one simulation step: flush commands, then run scheduled systems. */
    fun update(dt: Double) {
        commands.flush()
        for ((_, systems) in schedule.systems()) {
            for (system in systems) {
                system.update(this, dt)
            }
        }
    }

    /** Returns the storage for a component type, creating it if necessary. */
    @Suppress("UNCHECKED_CAST")
    fun <T : Component> storage(type: ComponentType<T>): ComponentStorage<T> =
        storages.getOrPut(type) { ComponentStorage(type) } as ComponentStorage<T>

    /** Returns all registered archetypes. */
    fun archetypes(): Collection<Archetype> = archetypes.values

    /** Returns the entities that currently belong to [archetype]. */
    fun entitiesIn(archetype: Archetype): List<Entity> {
        val result = ArrayList<Entity>()
        for ((i, element) in entitySlots.withIndex()) {
            if (entityArchetypes[i] == archetype.id && element.isOccupied) {
                result.add(Entity(i, element.generation.toInt()))
            }
        }
        return result
    }

    /** Returns the archetype id for an entity. */
    fun archetypeIdOf(entity: Entity): Int {
        if (!isAlive(entity)) return Archetype.EMPTY.id
        return entityArchetypes[entity.index]
    }

    private fun rebuildArchetype(index: Int) {
        val types = storages.entries
            .filter { it.value.has(index) }
            .map { it.key }
            .toSet()
        val archetype = Archetype.of(types)
        archetypes[archetype.id] = archetype
        entityArchetypes[index] = archetype.id
    }

    private fun componentTypeOf(component: Component): ComponentType<*> {
        // TODO: replace with a runtime type registry lookup once component types are registered.
        throw NotImplementedError(
            "Component type lookup by instance is not supported yet. " +
                "Use addComponent(entity, type, component) instead."
        )
    }

    /** Variant that lets callers specify the component type explicitly. */
    fun <T : Component> addComponent(entity: Entity, type: ComponentType<T>, component: T) {
        require(isAlive(entity)) { "Cannot add component to dead entity $entity" }
        val index = entity.index
        storage(type)[index] = component
        rebuildArchetype(index)
    }

    private class EntitySlot(
        val id: Int,
        var generation: Byte = -1,
        var isOccupied: Boolean = false
    )

    private class IntArrayList : ArrayList<Int>()

    companion object {
        fun builder(): WorldBuilder = WorldBuilder()
    }
}

class WorldBuilder {
    private val schedule = Schedule()

    fun system(group: SystemGroup, system: System): WorldBuilder {
        schedule.add(group, system)
        return this
    }

    fun build(): World = World(schedule)
}
