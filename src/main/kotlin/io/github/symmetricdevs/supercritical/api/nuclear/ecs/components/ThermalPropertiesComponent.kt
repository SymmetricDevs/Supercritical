package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component

/**
 * Thermal properties shared by every reactor cell component.
 *
 * Reconstructed from the component type and material stats during geometry rebuild.
 */
data class ThermalPropertiesComponent(
    var maxTemperature: Double = 0.0,
    var thermalConductivity: Double = 0.0,
    var mass: Double = 0.0
) : Component
