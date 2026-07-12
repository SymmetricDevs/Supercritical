package io.github.symmetricdevs.supercritical.api.nuclear.fission.components

class ControlRod(
    maxTemperature: Double,
    private val tipModeration: Boolean,
    thermalConductivity: Double,
    mass: Double
) : ReactorComponent(0.0, maxTemperature, thermalConductivity, mass, true) {
    var weight = 0.0
    private var relatedFuelRodPairs = 0

    override fun getAbsorptionFactor(controlsInserted: Boolean, thermal: Boolean): Double {
        return (if (controlsInserted) 4 else 0).toDouble()
    }

    fun addFuelRodPair() {
        relatedFuelRodPairs++
    }

    fun hasModeratorTip(): Boolean {
        return tipModeration
    }

    fun computeWeightFromFuelRodMap() {
        weight = (relatedFuelRodPairs * 4).toDouble()
    }


    companion object {
        fun normalizeWeights(effectiveControlRods: List<ControlRod>, totalWeight: Double, totalWorth: Double) {
            if (totalWeight == 0.0) return
            for (rod in effectiveControlRods) {
                rod.weight = rod.weight / totalWeight * totalWorth
            }
        }

        fun controlRodFactor(effectiveControlRods: List<ControlRod>, insertion: Double): Double {
            var factor = 0.0
            for (rod in effectiveControlRods) {
                if (rod.hasModeratorTip()) {
                    if (insertion <= 0.3) {
                        factor -= insertion / 3 * rod.weight
                    } else {
                        factor -= (-11.0 / 7 * (insertion - 0.3) + 0.1) * rod.weight
                    }
                } else {
                    factor += insertion * rod.weight
                }
            }
            return factor
        }
    }
}
