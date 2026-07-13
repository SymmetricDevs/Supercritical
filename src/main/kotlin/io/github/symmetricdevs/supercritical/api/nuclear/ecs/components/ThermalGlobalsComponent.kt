package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component

/**
 * Global thermal and coolant properties for the reactor.
 *
 * These are computed during geometry rebuild and precompute, not persisted.
 */
data class ThermalGlobalsComponent(
    var coolantBaseTemperature: Double = 0.0,
    var coolantBoilingPointStandardPressure: Double = 0.0,
    var coolantExitTemperature: Double = 0.0,
    var coolantHeatOfVaporization: Double = 0.0,
    var coolantMass: Double = 0.0,
    var structuralMass: Double = 0.0,
    var fuelMass: Double = 0.0,
    var surfaceArea: Double = 0.0,
    var exteriorPressure: Double = 101325.0,
    var envTemperature: Double = 273.0
) : Component
