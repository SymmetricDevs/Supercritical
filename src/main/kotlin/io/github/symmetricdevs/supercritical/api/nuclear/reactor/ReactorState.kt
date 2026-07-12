package io.github.symmetricdevs.supercritical.api.nuclear.reactor

import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor

/** Amplitude variables persisted for every core. Arrays live inside family-specific solvers. */
data class ReactorState(
    var neutronFlux: Double = 0.0,
    var power: Double = 0.0,
    var temperature: Double = FissionReactor.ROOM_TEMPERATURE,
    var prevTemperature: Double = FissionReactor.ROOM_TEMPERATURE,
    var pressure: Double = FissionReactor.STANDARD_PRESSURE,
    var fuelDepletion: Double = -1.0,
    var accumulatedHydrogen: Double = 0.0,
    var neutronPoisonAmount: Double = 0.0,
    var decayProductsAmount: Double = 0.0,
    var isOn: Boolean = false
)
