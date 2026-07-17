package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.ecs.Component
import io.github.symmetricdevs.supercritical.api.fission.stats.FissionFuelStats

/**
 * Fuel-rod cell data.
 */
data class FuelRodComponent(
    var fuel: FissionFuelStats,
    var weight: Double = 1.0,
    var thermalProportion: Double = 0.0
) : Component
