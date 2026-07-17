package io.github.symmetricdevs.supercritical.api.fission.ecs

/**
 * Deferred mutation buffer for a [io.github.symmetricdevs.supercritical.api.fission.ecs.World].
 *
 * Systems must not mutate entities while they are being iterated. Instead they
 * queue commands here; [io.github.symmetricdevs.supercritical.api.fission.ecs.World.flushCommands] applies them once per tick before
 * the next schedule run.
 */
class Commands(private val world: io.github.symmetricdevs.supercritical.api.fission.ecs.World) {

    private val queue = ArrayDeque<() -> Unit>()

    fun addComponent(entity: io.github.symmetricdevs.supercritical.api.fission.ecs.Entity, component: io.github.symmetricdevs.supercritical.api.fission.ecs.Component) {
        queue.addLast { world.addComponent(entity, component) }
    }

    fun removeComponent(entity: io.github.symmetricdevs.supercritical.api.fission.ecs.Entity, type: io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType<*>) {
        queue.addLast { world.removeComponent(entity, type) }
    }

    fun removeEntity(entity: io.github.symmetricdevs.supercritical.api.fission.ecs.Entity) {
        queue.addLast { world.destroyEntity(entity) }
    }

    internal fun flush() {
        while (queue.isNotEmpty()) {
            queue.removeFirst().invoke()
        }
    }

    fun isEmpty(): Boolean = queue.isEmpty()
}
