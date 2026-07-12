package io.github.symmetricdevs.supercritical.api.nuclear.fission.components

import io.github.symmetricdevs.supercritical.api.nuclear.fission.IModeratorStats

class Moderator(thermalConductivity: Double, mass: Double, val moderator: IModeratorStats) : ReactorComponent(
    moderator.moderationFactor, moderator.maxTemperature.toDouble(), thermalConductivity, mass, true
) {
    override fun getAbsorptionFactor(controlsInserted: Boolean, thermal: Boolean): Double {
        // Legacy Moderator.getAbsorptionFactor returns the moderator's absorption factor for
        // BOTH thermal and fast neutrons (it ignores isThermal). Returning 0 for fast neutrons
        // would drop moderator fast-neutron absorption and alter kEff.
        return moderator.absorptionFactor
    }
}
