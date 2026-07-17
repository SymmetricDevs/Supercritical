package io.github.symmetricdevs.supercritical.api.data.chemical.material.property

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import io.github.symmetricdevs.supercritical.api.fission.stats.CoolantStats
import net.minecraft.world.level.material.Fluid

data class CoolantProperty(
    val coolant: Material,
    val hotCoolant: Material,
    val hotCoolantKey: FluidStorageKey = FluidStorageKeys.LIQUID,
    val coolantKey: FluidStorageKey = FluidStorageKeys.GAS,
    override val moderatorFactor: Double,
    override val coolingFactor: Double,
    override val coolTemperature: Double,
    override val boilingPoint: Double,
    override val heatOfVaporization: Double,
    override val specificHeatCapacity: Double,
    override val accumulatesHydrogen: Boolean = false,
    override val slowAbsorptionFactor: Double = 0.0,
    override val fastAbsorptionFactor: Double = 0.0,
    override val mass: Double = coolant.mass.toDouble(),
) : IMaterialProperty, CoolantStats {

    override val coolantFluid: Fluid by lazy { coolant.getFluid(coolantKey) }
    override val hotCoolantFluid: Fluid by lazy { hotCoolant.getFluid(hotCoolantKey) }

    override fun verifyProperty(properties: MaterialProperties) {
        properties.ensureSet(PropertyKey.FLUID, true)
        hotCoolant.properties.ensureSet(PropertyKey.FLUID, true)
    }

}
