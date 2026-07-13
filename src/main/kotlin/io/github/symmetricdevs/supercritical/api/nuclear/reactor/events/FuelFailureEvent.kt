package io.github.symmetricdevs.supercritical.api.nuclear.reactor.events

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.ReactorEvent

enum class FuelFailureReason {
    CLOGGED,
    MISSING_FUEL
}

/**
 * Emitted by the fuel-handling system when a depleted fuel rod cannot be replaced.
 * The multiblock controller drains these events and updates the lock state.
 */
data class FuelFailureEvent(val reason: FuelFailureReason) : ReactorEvent
