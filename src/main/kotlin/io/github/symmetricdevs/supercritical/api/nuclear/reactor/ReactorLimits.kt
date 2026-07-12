package io.github.symmetricdevs.supercritical.api.nuclear.reactor

/** Structural limits are rebuilt from the multiblock, not persisted. */
data class ReactorLimits(
    var maxTemperature: Double = 2000.0,
    var maxPressure: Double = 15_000_000.0,
    var maxPower: Double = 3.0
)
