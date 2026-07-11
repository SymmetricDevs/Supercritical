package supercritical.data.recipe

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.data.recipe.CustomTags
import net.minecraft.world.item.ItemStack
import supercritical.common.data.ScritMachines
import supercritical.common.data.ScritMaterials
import supercritical.common.registry.ScritBlocks

object ScritMetaTileEntityLoader {
    fun init() {
        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fission_reactor")
                .inputItems(CustomTags.EV_CIRCUITS, 4)
                .inputItems(ItemStack(ScritBlocks.REACTOR_VESSEL.get()))
                .inputItems(GTItems.SENSOR_EV.asStack())
                .inputItems(TagPrefix.rotor, GTMaterials.Steel, 2)
                .inputItems(TagPrefix.wireGtQuadruple, GTMaterials.AnnealedCopper)
                .outputItems(ScritMachines.FISSION_REACTOR.asStack())
                .duration(400).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("heat_exchanger")
                .inputItems(CustomTags.HV_CIRCUITS)
                .inputItems(TagPrefix.pipeLargeFluid, ScritMaterials.Inconel, 2)
                .inputItems(ItemStack(GTBlocks.CASING_STEEL_SOLID.get(), 6))
                .outputItems(ScritMachines.HEAT_EXCHANGER.asStack())
                .duration(400).EUt(GTValues.VA[GTValues.HV].toLong())
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gas_centrifuge")
                .inputItems(CustomTags.EV_CIRCUITS, 3)
                .inputItems(TagPrefix.rotor, GTMaterials.Titanium)
                .inputItems(ItemStack(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get(), 3))
                .inputItems(TagPrefix.wireGtQuadruple, GTMaterials.AnnealedCopper, 2)
                .outputItems(ScritMachines.GAS_CENTRIFUGE.asStack())
                .duration(400).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("spent_fuel_pool")
                .inputItems(CustomTags.LV_CIRCUITS)
                .inputItems(ItemStack(GTBlocks.CASING_STAINLESS_CLEAN.get(), 2))
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 6)
                .outputItems(ScritMachines.SPENT_FUEL_POOL.asStack())
                .duration(400).EUt(GTValues.VA[GTValues.LV].toLong())
                .buildRawRecipe()
        )
    }
}
