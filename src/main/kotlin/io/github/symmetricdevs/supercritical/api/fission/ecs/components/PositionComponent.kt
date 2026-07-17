package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.ecs.Component

/**
 * Lattice position of a reactor cell entity.
 *
 * This is reconstructed from the lattice geometry on load and is not persisted.
 */
data class PositionComponent(
    var x: Int = 0,
    var y: Int = 0
) : Component
