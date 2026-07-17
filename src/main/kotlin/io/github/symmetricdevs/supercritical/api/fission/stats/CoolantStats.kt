package io.github.symmetricdevs.supercritical.api.fission.stats

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import io.github.symmetricdevs.supercritical.common.data.ScritPropertyKeys
import net.minecraft.world.level.material.Fluid

interface CoolantStats {
    val coolantFluid: Fluid

    val hotCoolantFluid: Fluid

    val coolTemperature: Double

    val specificHeatCapacity: Double

    val moderatorFactor: Double

    val slowAbsorptionFactor: Double

    val fastAbsorptionFactor: Double

    val coolingFactor: Double

    val boilingPoint: Double

    val heatOfVaporization: Double

    val accumulatesHydrogen: Boolean

    val mass: Double

    companion object {
        fun of(fluid: Fluid): CoolantStats? =
            ChemicalHelper.getMaterial(fluid)
                .takeIf { it.hasProperty(ScritPropertyKeys.COOLANT) }
                ?.getProperty(ScritPropertyKeys.COOLANT)
    }
}