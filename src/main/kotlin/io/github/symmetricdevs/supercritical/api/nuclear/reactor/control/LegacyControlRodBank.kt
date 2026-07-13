package io.github.symmetricdevs.supercritical.api.nuclear.reactor.control

import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ControlMechanism
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorCore
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorLimits
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorState
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.systems.cache
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.systems.controlRodFactor

/**
 * Control-rod bank backed by the reactor's ECS geometry cache rather than a hard cast to the
 * concrete [io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor].
 *
 * [worthCurve] and [regulate] have no external callers today (regulation runs through the
 * scheduled `ControlRodSystem`), so they simply defer to the shared [controlRodFactor] helper
 * and [ReactorCore.regulateControlRods].
 */
class LegacyControlRodBank(private val reactor: ReactorCore) : ControlMechanism {

    override var insertedFraction: Double
        get() = reactor.controlRodInsertion.coerceIn(0.0, 1.0)
        set(value) {
            reactor.controlRodInsertion = value.coerceIn(0.0, 1.0)
        }

    override val worthCurve: (fraction: Double) -> Double = { fraction: Double ->
        controlRodFactor(reactor.world, reactor.world.cache(), fraction.coerceIn(0.0, 1.0))
    }

    override fun regulate(state: ReactorState, limits: ReactorLimits, targetKeff: Double) {
        reactor.regulateControlRods()
    }
}
