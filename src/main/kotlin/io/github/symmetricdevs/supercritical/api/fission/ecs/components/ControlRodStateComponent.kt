package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.ecs.Component

/**
 * Control-rod bank state.
 */
data class ControlRodStateComponent(
    var insertion: Double = 0.0,
    var regulationOn: Boolean = true
) : Component
