package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component

/**
 * Neutronics properties shared by every reactor cell component.
 *
 * Reconstructed from the component type and material stats during geometry rebuild.
 */
data class NeutronicsPropertiesComponent(
    var moderationFactor: Double = 0.0,
    var absorptionFast: Double = 0.0,
    var absorptionSlow: Double = 0.0
) : Component
