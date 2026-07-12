package io.github.symmetricdevs.supercritical.api.nuclear.fission.components

import io.github.symmetricdevs.supercritical.api.capability.ICoolantHandler
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ICoolantStats

class CoolantChannel(maxTemperature: Double, thermalConductivity: Double, val coolant: ICoolantStats, mass: Double) :
    ReactorComponent(
        coolant.moderatorFactor, maxTemperature, thermalConductivity, mass, true
    ) {
    var weight: Double = 0.0
    var partialCoolant: Double = 0.0
    var inputHandler: ICoolantHandler? = null
        private set
    var outputHandler: ICoolantHandler? = null
        private set

    fun setHandlers(input: ICoolantHandler?, output: ICoolantHandler?) {
        this.inputHandler = input
        this.outputHandler = output
    }

    fun addWeight(weight: Double) {
        this.weight += weight
    }

    override fun getAbsorptionFactor(controlsInserted: Boolean, thermal: Boolean): Double {
        return if (thermal) coolant.slowAbsorptionFactor else coolant.fastAbsorptionFactor
    }
}
