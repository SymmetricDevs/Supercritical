package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component

/**
 * Structural limits for a reactor core.
 *
 * These are rebuilt from the multiblock geometry and component properties rather
 * than persisted.
 */
data class ReactorLimitsComponent(
    var maxTemperature: Double = 2000.0,
    var maxPressure: Double = 15_000_000.0,
    var maxPower: Double = 3.0
) : Component
