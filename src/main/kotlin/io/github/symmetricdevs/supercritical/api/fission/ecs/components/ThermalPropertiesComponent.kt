package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.ecs.Component

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
