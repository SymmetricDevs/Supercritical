package io.github.symmetricdevs.supercritical.api.fission.reactor

/** Any reactivity-control device. */
interface ControlMechanism {
    var insertedFraction: Double
    val worthCurve: (fraction: Double) -> Double

    fun reactivityWorth(): Double = worthCurve(insertedFraction)

    fun regulate(state: ReactorState, limits: ReactorLimits, targetKeff: Double = 1.0)
}
