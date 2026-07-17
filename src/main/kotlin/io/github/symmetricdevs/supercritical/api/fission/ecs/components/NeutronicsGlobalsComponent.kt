package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.ecs.Component

/**
 * Global neutronics constants updated by the eigenvalue precompute and point-kinetics solve.
 */
data class NeutronicsGlobalsComponent(
    var k: Double = 0.0,
    var kEff: Double = 0.0,
    var controlRodFactor: Double = 0.0,
    var neutronToPowerConversion: Double = 0.0,
    var decayNeutrons: Double = 0.0,
    var weightedGenerationTime: Double = 2.0
) : Component {
}
