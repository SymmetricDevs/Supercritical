package io.github.symmetricdevs.supercritical.api.fission.stats

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import io.github.symmetricdevs.supercritical.common.data.ScritPropertyKeys
import net.minecraft.world.item.ItemStack

interface FissionFuelStats {
    val maxTemperature: Double

    val duration: Int

    val slowNeutronCaptureCrossSection: Double

    val fastNeutronCaptureCrossSection: Double

    val slowNeutronFissionCrossSection: Double

    val fastNeutronFissionCrossSection: Double

    val releasedNeutrons: Double

    val requiredNeutrons: Double

    val releasedHeatEnergy: Double

    val decayRate: Double

    val neutronGenerationTime: Double

    val neutronGenerationTimeCategory: Int
        get() {
            if (neutronGenerationTime > 2) {
                return 0
            } else if (neutronGenerationTime > 1.25) {
                return 1
            } else if (neutronGenerationTime > 0.9) {
                return 2
            }
            return 3
        }

    val fastFissionMultiplier: Double
        get() = fastNeutronFissionCrossSection * releasedNeutrons / requiredNeutrons

    val slowFissionMultiplier: Double
        get() = slowNeutronFissionCrossSection * releasedNeutrons / requiredNeutrons

    val id: String?

    val depletedFuels: MutableList<ItemStack>

    fun getDepletedFuel(thermalRatio: Double): ItemStack

    companion object {
        /** The fuel stats for [stack], or null if the stack's material has no fission-fuel property. */
        fun of(stack: ItemStack): FissionFuelStats? {
            if (stack.isEmpty) return null
            val material = ChemicalHelper.getMaterialStack(stack).material()
            if (!material.hasProperty(ScritPropertyKeys.FISSION_FUEL)) return null
            return material.getProperty(ScritPropertyKeys.FISSION_FUEL)
        }

        /** The fuel stats for a material id (`"namespace:path"`, as persisted from [id]), or null. */
        fun of(id: String): FissionFuelStats? {
            val material = GTCEuAPI.materialManager.getMaterial(id)
            if (!material.hasProperty(ScritPropertyKeys.FISSION_FUEL)) return null
            return material.getProperty(ScritPropertyKeys.FISSION_FUEL)
        }
    }
}
