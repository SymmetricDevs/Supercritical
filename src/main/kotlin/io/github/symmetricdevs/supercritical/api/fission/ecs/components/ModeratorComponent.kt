package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.stats.ModeratorStats
import io.github.symmetricdevs.supercritical.api.fission.ecs.Component

/**
 * Moderator cell data.
 */
data class ModeratorComponent(
    var moderator: ModeratorStats
) : Component
