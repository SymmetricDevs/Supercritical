package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component

/**
 * Control-rod cell data.
 */
data class ControlRodComponent(
    var tipModeration: Boolean = false,
    var weight: Double = 0.0,
    var relatedFuelRodPairs: Int = 0
) : Component
