package io.github.symmetricdevs.supercritical.api.fission.ecs.resources

import io.github.symmetricdevs.supercritical.api.capability.CoolantHandler
import io.github.symmetricdevs.supercritical.api.fission.ecs.Resource

/**
 * Maps each coolant-channel lattice cell to its import (and optional export) hatch.
 *
 * This bridge is populated by the multiblock controller when the reactor is formed
 * and consumed by the thermal-hydraulics system. Keeping hatch references in a
 * resource instead of inside the coolant-channel ECS component lets the component
 * stay plain data and makes the coolant flow code family-agnostic.
 */
class CoolantInventoryBridge : Resource {
    private val handlers = linkedMapOf<Pair<Int, Int>, Pair<CoolantHandler, CoolantHandler?>>()

    fun register(x: Int, y: Int, input: CoolantHandler, output: CoolantHandler?) {
        handlers[x to y] = input to output
    }

    operator fun get(x: Int, y: Int): Pair<CoolantHandler, CoolantHandler?>? = handlers[x to y]

    fun entries(): Set<Map.Entry<Pair<Int, Int>, Pair<CoolantHandler, CoolantHandler?>>> = handlers.entries
}
