package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.ecs.Component

/**
 * Global reactor physics state.
 *
 * This replaces the mutable fields that lived directly on [FissionReactor] and
 * the family-agnostic [ReactorState] snapshot data class.
 */
data class ReactorStateComponent(
    var neutronFlux: Double = 0.0,
    var power: Double = 0.0,
    var temperature: Double = 273.0,
    var prevTemperature: Double = 0.0,
    var pressure: Double = 101325.0,
    var fuelDepletion: Double = -1.0,
    var accumulatedHydrogen: Double = 0.0,
    var neutronPoisonAmount: Double = 0.0,
    var decayProductsAmount: Double = 0.0,
    var isOn: Boolean = false
) : Component
