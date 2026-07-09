package supercritical.api.nuclear.fission.components

import supercritical.api.nuclear.fission.IModeratorStats

class Moderator(thermalConductivity: Double, mass: Double, val moderator: IModeratorStats) : ReactorComponent(
    moderator.getModerationFactor(), moderator.getMaxTemperature().toDouble(), thermalConductivity, mass, true
) {
    override fun getAbsorptionFactor(controlsInserted: Boolean, thermal: Boolean): Double {
        return if (thermal) moderator.getAbsorptionFactor() else 0.0
    }
}
