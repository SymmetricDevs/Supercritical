package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component
import io.github.symmetricdevs.supercritical.api.nuclear.fission.IFissionFuelStats

/**
 * Fuel-rod cell data.
 */
data class FuelRodComponent(
    var fuel: IFissionFuelStats,
    var weight: Double = 1.0,
    var thermalProportion: Double = 0.0
) : Component
