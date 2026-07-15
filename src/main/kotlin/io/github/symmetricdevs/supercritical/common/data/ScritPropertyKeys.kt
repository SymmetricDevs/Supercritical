package io.github.symmetricdevs.supercritical.common.data

import io.github.symmetricdevs.supercritical.api.data.chemical.material.property.CoolantProperty
import io.github.symmetricdevs.supercritical.api.data.chemical.material.property.FissionFuelProperty
import io.github.symmetricdevs.supercritical.api.data.chemical.material.property.ModeratorProperty
import io.github.symmetricdevs.supercritical.util.PropertyKey

object ScritPropertyKeys {
    val COOLANT = PropertyKey<CoolantProperty>("coolant")
    val FISSION_FUEL = PropertyKey<FissionFuelProperty>("fission_fuel")
    val MODERATOR = PropertyKey<ModeratorProperty>("moderator")
}