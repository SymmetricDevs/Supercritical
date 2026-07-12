package io.github.symmetricdevs.supercritical.api.nuclear.reactor.control

import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.ControlRod
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ControlMechanism
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorLimits
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorState
import kotlin.math.max
import kotlin.math.min

class LegacyControlRodBank(private val reactor: FissionReactor) : ControlMechanism {

    override var insertedFraction: Double
        get() = reactor.controlRodInsertion.coerceIn(0.0, 1.0)
        set(value) {
            reactor.controlRodInsertion = value.coerceIn(0.0, 1.0)
        }

    override val worthCurve: (fraction: Double) -> Double = { fraction: Double ->
        ControlRod.controlRodFactor(reactor.effectiveControlRods, fraction.coerceIn(0.0, 1.0))
    }

    override fun regulate(state: ReactorState, limits: ReactorLimits, targetKeff: Double) {
        if (!reactor.isOn || !reactor.controlRodRegulationOn) return

        var adjustFactor = false
        val pressure = reactor.pressure
        val temperature = reactor.temperature
        val prevTemperature = state.prevTemperature
        val maxPressure = limits.maxPressure
        val maxTemperature = limits.maxTemperature
        val kEff = reactor.kEff
        val coolantExitTemperature = reactor.coolantExitTemperature
        val coolantBaseTemperature = reactor.coolantBaseTemperature

        if (pressure > maxPressure * 0.8 || temperature > (coolantExitTemperature + maxTemperature) / 2 || temperature > maxTemperature - 150 || temperature - prevTemperature > 30) {
            if (kEff > 0.99) {
                reactor.controlRodInsertion += 0.004
                adjustFactor = true
            }
        } else if (temperature > coolantExitTemperature * 0.3 + coolantBaseTemperature * 0.7) {
            if (kEff > 1.01) {
                reactor.controlRodInsertion += 0.008
                adjustFactor = true
            } else if (kEff < 1.005) {
                reactor.controlRodInsertion -= 0.001
                adjustFactor = true
            }
        } else if (temperature > coolantExitTemperature * 0.1 + coolantBaseTemperature * 0.9) {
            if (kEff > 1.025) {
                reactor.controlRodInsertion += 0.012
                adjustFactor = true
            } else if (kEff < 1.015) {
                reactor.controlRodInsertion -= 0.004
                adjustFactor = true
            }
        } else {
            if (kEff > 1.1) {
                reactor.controlRodInsertion += 0.02
                adjustFactor = true
            } else if (kEff < 1.05) {
                reactor.controlRodInsertion -= 0.006
                adjustFactor = true
            }
        }

        if (adjustFactor) {
            reactor.controlRodInsertion = max(0.0, min(1.0, reactor.controlRodInsertion))
            reactor.updateControlRodInsertion(reactor.controlRodInsertion)
        }
    }
}
