package supercritical.data.recipe

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import supercritical.common.data.ScritMaterials
import supercritical.common.data.ScritBlocks

object ScritMiscRecipes {
    fun init() {
        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gray_panelling")
                .inputItems(TagPrefix.plate, GTMaterials.Steel, 4)
                .circuitMeta(16)
                .outputItems(ItemStack(ScritBlocks.GRAY_PANELLING.get()))
                .EUt(16).duration(120)
                .buildRawRecipe()
        )

        for (color in DyeColor.entries) {
            ScritRecipeUtils.addRecipe(
                GTRecipeTypes.CHEMICAL_BATH_RECIPES,
                GTRecipeTypes.CHEMICAL_BATH_RECIPES.recipeBuilder(color.getName() + "_panelling")
                    .inputItems(ItemStack(ScritBlocks.GRAY_PANELLING.get()))
                    .inputFluids(GTMaterials.CHEMICAL_DYES[color.ordinal].getFluid(9))
                    .outputItems(ItemStack(ScritBlocks.PANELLING.getValue(color).get()))
                    .EUt(2).duration(10)
                    .buildRawRecipe()
            )
        }

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.CHEMICAL_RECIPES, GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder("heavy_water")
                .inputFluids(GTMaterials.Deuterium.getFluid(2000), GTMaterials.Oxygen.getFluid(1000))
                .outputFluids(ScritMaterials.HeavyWater.getFluid(1000))
                .duration(200).EUt(GTValues.VH[GTValues.LV].toLong())
                .buildRawRecipe()
        )
    }
}
