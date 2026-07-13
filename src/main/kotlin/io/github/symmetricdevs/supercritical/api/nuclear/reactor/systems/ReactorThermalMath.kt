package io.github.symmetricdevs.supercritical.api.nuclear.reactor.systems

import io.github.symmetricdevs.supercritical.api.nuclear.fission.CoolantRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ICoolantStats
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorPhysics
import io.github.symmetricdevs.supercritical.config.ScritConfig

// Shared PWR coolant/thermal math, hoisted from the legacy thermal-hydraulics
// implementation so both the precompute and the
// per-tick thermal systems evaluate the same expressions. These are pure
// functions of the coolant stats + reactor geometry/scalar globals.

/** Boiling point of [coolant], falling back to the standard-pressure value if unset. */
internal fun coolantBoilingPoint(coolant: ICoolantStats, standardPressureValue: Double): Double =
    if (coolant.boilingPoint == 0.0) standardPressureValue else coolant.boilingPoint

/** Inlet (cold) temperature of [prop], from its original fluid or room temperature. */
internal fun coolantInletTemp(prop: ICoolantStats): Int {
    val original = CoolantRegistry.originalFluid(prop)
    return if (original == null) ReactorPhysics.ROOM_TEMPERATURE.toInt() else original.fluidType.temperature
}

/** Heat removed per liter of coolant flowing from [coolantTemp] to [cooledTemperature]. */
internal fun heatRemovedPerLiter(prop: ICoolantStats, coolantTemp: Int, cooledTemperature: Int): Double =
    prop.specificHeatCapacity / ScritConfig.INSTANCE.nuclear.fissionCoolantDivisor * (cooledTemperature - coolantTemp)

/** Convective heat flux per unit area through the coolant-channel wall. */
internal fun coolantHeatFluxPerArea(prop: ICoolantStats): Double =
    1 / (1 / prop.coolingFactor + ReactorPhysics.coolantWallThickness / ReactorPhysics.thermalConductivity)

/** Ideal (uncapped) heat flux a coolant channel of [weight] carries at [refTemp] → [cooledTemperature]. */
internal fun idealCoolantHeatFlux(
    prop: ICoolantStats, weight: Double, depth: Int, refTemp: Double, cooledTemperature: Int
): Double = coolantHeatFluxPerArea(prop) * weight * depth * (refTemp - cooledTemperature)

/** Thermal time constant of the reactor structure for the temperature response function. */
internal fun thermalTimeConstant(surfaceArea: Double): Double =
    ReactorPhysics.specificHeatCapacity *
        (1 / ReactorPhysics.convectiveHeatTransferCoefficient + ReactorPhysics.wallThickness / ReactorPhysics.thermalConductivity) /
        surfaceArea
