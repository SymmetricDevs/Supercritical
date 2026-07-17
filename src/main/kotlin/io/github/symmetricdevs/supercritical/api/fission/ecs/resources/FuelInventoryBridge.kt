package io.github.symmetricdevs.supercritical.api.fission.ecs.resources

import io.github.symmetricdevs.supercritical.api.capability.FuelRodHandler
import io.github.symmetricdevs.supercritical.api.fission.ecs.Resource

/**
 * Maps each fuel-rod lattice cell to its import hatch.
 *
 * This bridge is populated by the multiblock controller when the reactor is formed
 * and consumed by the fuel-handling system. The bridge also carries the reactor
 * [depth] so the system can reach the matching export hatch for depleted fuel.
 */
class FuelInventoryBridge(var depth: Int = 1) : Resource {
    private val handlers = linkedMapOf<Pair<Int, Int>, FuelRodHandler>()

    fun register(x: Int, y: Int, handler: FuelRodHandler) {
        handlers[x to y] = handler
    }

    operator fun get(x: Int, y: Int): FuelRodHandler? = handlers[x to y]

    fun entries(): Set<Map.Entry<Pair<Int, Int>, FuelRodHandler>> = handlers.entries

    fun clear() = handlers.clear()
}
