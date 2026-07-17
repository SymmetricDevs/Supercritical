package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.ecs.Component

/**
 * Control-rod cell data.
 */
data class ControlRodComponent(
    var tipModeration: Boolean = false,
    var weight: Double = 0.0,
    var relatedFuelRodPairs: Int = 0
) : Component
