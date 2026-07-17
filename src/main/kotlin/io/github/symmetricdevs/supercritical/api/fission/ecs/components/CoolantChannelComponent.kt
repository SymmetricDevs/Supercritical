package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.stats.CoolantStats
import io.github.symmetricdevs.supercritical.api.fission.ecs.Component

/**
 * Coolant-channel cell data.
 *
 * Hatch references are intentionally not stored here; they live in
 * [io.github.symmetricdevs.supercritical.api.fission.ecs.resources.CoolantInventoryBridge]
 * and are mapped by cell position.
 */
data class CoolantChannelComponent(
    var coolant: CoolantStats,
    var weight: Double = 0.0,
    var partialCoolant: Double = 0.0
) : Component
