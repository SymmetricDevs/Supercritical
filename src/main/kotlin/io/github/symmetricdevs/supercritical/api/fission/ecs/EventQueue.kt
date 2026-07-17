package io.github.symmetricdevs.supercritical.api.fission.ecs

/**
 * Single-producer/single-consumer queue for cross-boundary events.
 *
 * Events are typed and processed by a dedicated system (or flushed at the start
 * of the tick alongside [Commands]). This is useful for things like fuel
 * replacement or hatch lock/unlock that originate outside the simulation.
 */
class EventQueue<E : ReactorEvent> {
    private val queue = ArrayDeque<E>()

    fun emit(event: E) {
        queue.addLast(event)
    }

    fun poll(): E? = queue.removeFirstOrNull()

    fun peek(): E? = queue.firstOrNull()

    fun isEmpty(): Boolean = queue.isEmpty()

    fun clear() = queue.clear()

    fun asSequence(): Sequence<E> = generateSequence { poll() }
}

/**
 * Marker interface for events that can be queued in an [EventQueue].
 */
interface ReactorEvent
