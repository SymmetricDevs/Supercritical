package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ICoolantStats

/**
 * Coolant-channel cell data.
 *
 * Hatch references are intentionally not stored here; they live in
 * [io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources.CoolantInventoryBridge]
 * and are mapped by cell position.
 */
data class CoolantChannelComponent(
    var coolant: ICoolantStats,
    var weight: Double = 0.0,
    var partialCoolant: Double = 0.0
) : Component
