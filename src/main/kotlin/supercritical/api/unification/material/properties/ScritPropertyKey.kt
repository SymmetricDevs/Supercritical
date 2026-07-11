package supercritical.api.unification.material.properties

import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey

object ScritPropertyKey {
    val COOLANT: PropertyKey<CoolantProperty?> = PropertyKey<CoolantProperty?>("coolant", CoolantProperty::class.java)
    val FISSION_FUEL: PropertyKey<FissionFuelProperty?> =
        PropertyKey<FissionFuelProperty?>("fission_fuel", FissionFuelProperty::class.java)
    val MODERATOR: PropertyKey<ModeratorProperty?> =
        PropertyKey<ModeratorProperty?>("moderator", ModeratorProperty::class.java)
}
