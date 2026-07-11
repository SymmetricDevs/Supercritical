package supercritical.data.recipe

import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.config.ConfigHolder
import net.minecraft.world.item.ItemStack
import supercritical.common.data.ScritMaterials
import supercritical.common.data.ScritBlocks

object ScritMachineRecipeLoader {
    fun init() {
        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("reactor_vessel")
                .inputItems(TagPrefix.plateDouble, ScritMaterials.Inconel)
                .inputItems(TagPrefix.plate, GTMaterials.Steel, 5)
                .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
                .outputItems(ItemStack(ScritBlocks.REACTOR_VESSEL.get(), ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .EUt(48).duration(280)
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("coolant_channel")
                .inputItems(TagPrefix.pipeLargeFluid, ScritMaterials.Inconel)
                .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
                .outputItems(ItemStack(ScritBlocks.COOLANT_CHANNEL.get()))
                .EUt(48).duration(280)
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fuel_channel")
                .inputItems(TagPrefix.rod, ScritMaterials.Zircaloy, 6)
                .inputItems(TagPrefix.ring, ScritMaterials.Zircaloy)
                .circuitMeta(1)
                .outputItems(ItemStack(ScritBlocks.FUEL_CHANNEL.get()))
                .EUt(48).duration(280)
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("control_rod_channel")
                .inputItems(TagPrefix.rod, ScritMaterials.Zircaloy, 3)
                .inputItems(TagPrefix.ring, ScritMaterials.Zircaloy)
                .circuitMeta(2)
                .outputItems(ItemStack(ScritBlocks.CONTROL_ROD_CHANNEL.get()))
                .EUt(48).duration(280)
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gas_centrifuge_heater")
                .inputItems(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.asStack())
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.Nichrome, 4)
                .outputItems(ItemStack(ScritBlocks.GAS_CENTRIFUGE_HEATER.get()))
                .EUt(48).duration(200)
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gas_centrifuge_column")
                .inputItems(TagPrefix.pipeNormalFluid, GTMaterials.Steel)
                .inputItems(TagPrefix.pipeTinyFluid, GTMaterials.Steel, 3)
                .outputItems(ItemStack(ScritBlocks.GAS_CENTRIFUGE_COLUMN.get()))
                .EUt(48).duration(200)
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("spent_fuel_casing")
                .inputItems(TagPrefix.rod, ScritMaterials.BoronCarbide, 8)
                .outputItems(ItemStack(ScritBlocks.SPENT_FUEL_CASING.get(), ConfigHolder.INSTANCE.recipes.casingsPerCraft))
                .EUt(64).duration(200)
                .buildRawRecipe()
        )
    }
}
