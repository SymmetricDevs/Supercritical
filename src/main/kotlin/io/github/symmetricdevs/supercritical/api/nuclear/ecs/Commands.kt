package io.github.symmetricdevs.supercritical.api.nuclear.ecs

/**
 * Deferred mutation buffer for a [World].
 *
 * Systems must not mutate entities while they are being iterated. Instead they
 * queue commands here; [World.flushCommands] applies them once per tick before
 * the next schedule run.
 */
class Commands(private val world: World) {

    private val queue = ArrayDeque<() -> Unit>()

    fun addComponent(entity: Entity, component: Component) {
        queue.addLast { world.addComponent(entity, component) }
    }

    fun removeComponent(entity: Entity, type: ComponentType<*>) {
        queue.addLast { world.removeComponent(entity, type) }
    }

    fun removeEntity(entity: Entity) {
        queue.addLast { world.destroyEntity(entity) }
    }

    internal fun flush() {
        while (queue.isNotEmpty()) {
            queue.removeFirst().invoke()
        }
    }

    fun isEmpty(): Boolean = queue.isEmpty()
}
