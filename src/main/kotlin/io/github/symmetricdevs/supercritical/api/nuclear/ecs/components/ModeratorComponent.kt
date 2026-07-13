package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component
import io.github.symmetricdevs.supercritical.api.nuclear.fission.IModeratorStats

/**
 * Moderator cell data.
 */
data class ModeratorComponent(
    var moderator: IModeratorStats
) : Component
