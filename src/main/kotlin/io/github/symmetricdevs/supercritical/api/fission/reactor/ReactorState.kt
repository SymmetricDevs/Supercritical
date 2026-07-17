package io.github.symmetricdevs.supercritical.api.fission.reactor

/**
 * Amplitude variables persisted for every core. Arrays live inside family-specific solvers.
 *
 * The temperature/pressure defaults are physical constants (273.0 K = 0 °C, 101325.0 Pa = 1 atm)
 * inlined here rather than referenced from the PWR [io.github.symmetricdevs.supercritical.api.fission.reactor.pwr.PWRCore]
 * constants, so the family-agnostic reactor package does not compile-time depend on one specific family.
 */
data class ReactorState(
    var neutronFlux: Double = 0.0,
    var power: Double = 0.0,
    var temperature: Double = 273.0,
    var prevTemperature: Double = 273.0,
    var pressure: Double = 101325.0,
    var fuelDepletion: Double = -1.0,
    var accumulatedHydrogen: Double = 0.0,
    var neutronPoisonAmount: Double = 0.0,
    var decayProductsAmount: Double = 0.0,
    var isOn: Boolean = false
)
