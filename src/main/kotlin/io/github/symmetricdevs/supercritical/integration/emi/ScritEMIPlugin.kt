package io.github.symmetricdevs.supercritical.integration.emi

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.stack.EmiStack
import io.github.symmetricdevs.supercritical.api.data.chemical.material.property.CoolantProperty
import io.github.symmetricdevs.supercritical.common.data.ScritItems
import io.github.symmetricdevs.supercritical.common.data.ScritPropertyKeys
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.emi.basic.*
import io.github.symmetricdevs.supercritical.integration.xei.CoolantInfo
import io.github.symmetricdevs.supercritical.integration.xei.FissionFuelInfo
import io.github.symmetricdevs.supercritical.integration.xei.ModeratorInfo

/**
 * EMI integration. Discovered via the [@EmiEntrypoint] annotation, so this class must not be
 * referenced from the main mod class — EMI loads it only when present.
 *
 * Recipes are thin wrappers over LDLib's [com.lowdragmc.lowdraglib.emi.ModularEmiRecipe], which
 * auto-extracts the shared [io.github.symmetricdevs.supercritical.integration.xei.widgets] slots.
 */
@EmiEntrypoint
class ScritEMIPlugin : EmiPlugin {

    override fun register(registry: EmiRegistry) {
        registry.addCategory(CoolantEmiCategory)
        registry.addCategory(FissionFuelEmiCategory)
        registry.addCategory(ModeratorEmiCategory)

        for (material in GTCEuAPI.materialManager.registeredMaterials) {
            if (!material.hasProperty(ScritPropertyKeys.COOLANT)) continue
            val property = material.getProperty(ScritPropertyKeys.COOLANT)
            val coolant = property.coolantFluid
            val hotCoolant = property.hotCoolantFluid
            registry.addRecipe(CoolantEmiRecipe(CoolantInfo(coolant, hotCoolant)))
        }
        for (fuelItems in ScritItems.NUCLEAR_FUEL_ITEMS.values) {
            registry.addRecipe(FissionFuelEmiRecipe(FissionFuelInfo(fuelItems.fuelRod.get().defaultInstance)))
        }
        for (material in GTCEuAPI.materialManager.registeredMaterials) {
            if (!material.hasProperty(ScritPropertyKeys.MODERATOR)) continue
            val block = ChemicalHelper.getBlock(TagPrefix.block, material) ?: continue
            registry.addRecipe(ModeratorEmiRecipe(ModeratorInfo(block)))
        }
    }
}
