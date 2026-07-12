package io.github.symmetricdevs.supercritical.data.recipe

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import io.github.symmetricdevs.supercritical.common.data.ScritBlocks
import io.github.symmetricdevs.supercritical.common.data.ScritItems
import io.github.symmetricdevs.supercritical.common.data.ScritMaterials
import io.github.symmetricdevs.supercritical.util.outputFluids
import io.github.symmetricdevs.supercritical.util.scId
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.world.item.DyeColor
import java.util.function.Consumer

object MiscRecipes {
    fun init(provider: Consumer<FinishedRecipe>) {
        // Panellings
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("gray_panelling"))
            .inputItems(TagPrefix.plate, GTMaterials.Steel, 4)
            .circuitMeta(16)
            .outputItems(ScritBlocks.GRAY_PANELLING)
            .EUt(16).duration(120)
            .save(provider)

        DyeColor.entries.forEach { color ->
            GTRecipeTypes.CHEMICAL_BATH_RECIPES.recipeBuilder(scId(color.getName() + "_panelling"))
                .inputItems(ScritBlocks.GRAY_PANELLING)
                .inputFluids(GTMaterials.CHEMICAL_DYES[color.ordinal], 9)
                .outputItems(ScritBlocks.PANELLING[color]!!)
                .EUt(2).duration(10)
                .save(provider)
        }

        GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(scId("heavy_water"))
            .inputFluids(GTMaterials.Deuterium, 2000)
            .inputFluids(GTMaterials.Oxygen, 1000)
            .outputFluids(ScritMaterials.HeavyWater, 1000)
            .duration(200).EUt(GTValues.VH[GTValues.LV].toLong())
            .save(provider)

        // Items
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("anode_basket"))
            .inputItems(TagPrefix.ring, GTMaterials.Titanium, 2)
            .inputItems(TagPrefix.rod, GTMaterials.Titanium, 16)
            .outputItems(ScritItems.ANODE_BASKET)
            .duration(400).EUt(GTValues.VA[GTValues.LV].toLong())
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("fuel_cladding"))
            .inputItems(TagPrefix.plate, ScritMaterials.Zircaloy, 4)
            .inputItems(TagPrefix.spring, ScritMaterials.Inconel)
            .inputItems(TagPrefix.round, GTMaterials.StainlessSteel, 2)
            .outputItems(ScritItems.FUEL_CLADDING)
            .duration(200).EUt(GTValues.VA[GTValues.MV].toLong())
            .save(provider)
    }
}
