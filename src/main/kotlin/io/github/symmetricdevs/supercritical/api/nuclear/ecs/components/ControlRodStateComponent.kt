package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component

/**
 * Control-rod bank state.
 */
data class ControlRodStateComponent(
    var insertion: Double = 0.0,
    var regulationOn: Boolean = true
) : Component
