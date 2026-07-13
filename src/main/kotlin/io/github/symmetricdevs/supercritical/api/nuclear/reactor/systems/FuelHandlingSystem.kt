package io.github.symmetricdevs.supercritical.api.nuclear.reactor.systems

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.System
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.World
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources.FuelInventoryBridge
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources.ReactorCoreResource
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.events.FuelFailureEvent
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.events.FuelFailureReason

/**
 * Fuel replacement system.
 *
 * Iterates the [FuelInventoryBridge] and swaps depleted fuel rods for fresh ones
 * via the hatch inventories. If a rod cannot be replaced (clogged output or empty
 * input), it shuts the reactor down and emits a [FuelFailureEvent] for the
 * controller to surface as a locking-state error.
 */
class FuelHandlingSystem : System {
    override fun update(world: World, dt: Double) {
        val bridge = world.resources.get<FuelInventoryBridge>() ?: return
        val core = world.resources.get<ReactorCoreResource>()?.core ?: return
        if (!core.isOn) return

        var canWork = true
        for ((_, fuelImport) in bridge.entries()) {
            if (!fuelImport.isDepleted(core.fuelDepletion)) continue

            val output = fuelImport.getOutputStackHandler(bridge.depth)
            if (output == null || !output.insertItem(0, fuelImport.depletedFuel, true).isEmpty) {
                world.eventQueue<FuelFailureEvent>().emit(FuelFailureEvent(FuelFailureReason.CLOGGED))
                canWork = false
                break
            }
            output.insertItem(0, fuelImport.depletedFuel, false)
            fuelImport.markUndepleted()

            val input = fuelImport.inputStackHandler
            if (input == null || input.extractItem(0, 1, true).isEmpty) {
                fuelImport.setPartialFuel(null)
                world.eventQueue<FuelFailureEvent>().emit(FuelFailureEvent(FuelFailureReason.MISSING_FUEL))
                canWork = false
                break
            }
            input.extractItem(0, 1, false)
        }

        if (!canWork) {
            core.isOn = false
        }
    }
}
