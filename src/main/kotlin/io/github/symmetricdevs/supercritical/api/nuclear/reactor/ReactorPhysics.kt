package io.github.symmetricdevs.supercritical.api.nuclear.reactor

import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Shared physics constants and math helpers for reactor simulations.
 *
 * These were originally companion members of [io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor];
 * they are hoisted here so ECS systems and legacy kernels can share them without
 * reaching back into the concrete reactor class.
 */
object ReactorPhysics {

    const val R: Double = 8.31446261815324
    const val STANDARD_PRESSURE: Double = 101325.0
    const val ROOM_TEMPERATURE: Double = 273.0
    const val AIR_BOILING_POINT: Double = 78.8

    var thermalConductivity: Double = 45.0 // W/(m K), for steel
    var wallThickness: Double = 0.1 // m
    var coolantWallThickness: Double = 0.02 // m (legacy value; 0.06 was the original, then /3 for balance)
    var specificHeatCapacity: Double = 420.0 // J/(kg K), for steel
    var convectiveHeatTransferCoefficient: Double = 10.0 // W/(m^2 K), for slow-moving air
    var powerDefectCoefficient: Double = 0.016 // reactivity units
    var decayProductRate: Double =
        0.997 // based on the half-life of xenon-135, using real-life days as Minecraft days
    var poisonFraction: Double = 0.063 // xenon-135 yield from fission
    var crossSectionRatio: Double = 4.0 // ratio between the cross-section for typical fuels and xenon-135
    var zircaloyHydrogenReactionTemperature: Double = 1500.0 // K

    fun responseFunction(target: Double, current: Double, criticalRate: Double): Double {
        var current = current
        if (current < 0) {
            if (criticalRate < 1) return 0.0
            current = 0.1
        }
        val expDecay = exp(-criticalRate)
        return current * expDecay + target * (1 - expDecay)
    }

    fun getMagnitude(vector: DoubleArray): Double {
        var magnitude = 0.0
        for (component in vector) magnitude += component * component
        return sqrt(magnitude)
    }

    fun normalize(vector: DoubleArray) {
        val magnitude: Double = getMagnitude(vector)
        if (magnitude == 0.0) return
        for (i in vector.indices) vector[i] /= magnitude
    }

    fun linearNormalize(vector: DoubleArray) {
        var sum = 0.0
        for (component in vector) sum += component
        if (sum == 0.0) return
        for (i in vector.indices) vector[i] /= sum
    }

    fun multiply(matrix: Array<DoubleArray>, vector: DoubleArray) {
        val result = DoubleArray(vector.size)
        for (i in matrix.indices) {
            for (j in matrix[i].indices) {
                result[i] += matrix[i][j] * vector[j]
            }
        }
        System.arraycopy(result, 0, vector, 0, result.size)
    }
}
