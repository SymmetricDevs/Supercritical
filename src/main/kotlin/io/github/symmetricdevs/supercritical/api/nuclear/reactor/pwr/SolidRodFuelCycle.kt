package io.github.symmetricdevs.supercritical.api.nuclear.reactor.pwr

import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.FuelCycle
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorState
import net.minecraft.nbt.CompoundTag

/**
 * Legacy solid-fuel-rod burnup: `fuelDepletion += flux * depth` per tick.
 *
 * Wraps the single depletion accumulation that lived inside legacy `updatePower` (called with
 * `flux = reactor.neutronFlux` after the point-kinetics flux evolution, `dt = reactorDepth`).
 *
 * `fuelDepletion` itself stays on the reactor - it is part of
 * [io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorCore] and is persisted by the
 * reactor's serialize/deserialize - so this kernel holds no persistent state of its own.
 */
class SolidRodFuelCycle(private val reactor: FissionReactor) : FuelCycle {

    override fun step(state: ReactorState, flux: Double, dt: Double) {
        reactor.fuelDepletion += flux * dt
    }

    override fun save(tag: CompoundTag) {
        // fuelDepletion is persisted by the reactor (ReactorCore.save).
    }

    override fun load(tag: CompoundTag) {
        // fuelDepletion is restored by the reactor (ReactorCore.load).
    }
}
