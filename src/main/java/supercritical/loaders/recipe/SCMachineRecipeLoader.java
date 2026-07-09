package supercritical.loaders.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import supercritical.api.unification.material.SCMaterials;
import supercritical.common.registry.SCBlocks;

import net.minecraft.world.item.ItemStack;

import static supercritical.loaders.recipe.SCRecipeUtils.addRecipe;

public final class SCMachineRecipeLoader {

    private SCMachineRecipeLoader() {}

    public static void init() {
        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("reactor_vessel")
                .inputItems(TagPrefix.plateDouble, SCMaterials.Inconel)
                .inputItems(TagPrefix.plate, GTMaterials.Steel, 5)
                .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
                .outputItems(new ItemStack(SCBlocks.REACTOR_VESSEL.get(), 2))
                .EUt(48).duration(280)
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("coolant_channel")
                .inputItems(TagPrefix.pipeLargeFluid, SCMaterials.Inconel)
                .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
                .outputItems(new ItemStack(SCBlocks.COOLANT_CHANNEL.get()))
                .EUt(48).duration(280)
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fuel_channel")
                .inputItems(TagPrefix.rod, SCMaterials.Zircaloy, 6)
                .inputItems(TagPrefix.ring, SCMaterials.Zircaloy)
                .circuitMeta(1)
                .outputItems(new ItemStack(SCBlocks.FUEL_CHANNEL.get()))
                .EUt(48).duration(280)
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("control_rod_channel")
                .inputItems(TagPrefix.rod, SCMaterials.Zircaloy, 3)
                .inputItems(TagPrefix.ring, SCMaterials.Zircaloy)
                .circuitMeta(2)
                .outputItems(new ItemStack(SCBlocks.CONTROL_ROD_CHANNEL.get()))
                .EUt(48).duration(280)
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gas_centrifuge_heater")
                .inputItems(TagPrefix.pipeLargeFluid, GTMaterials.Polytetrafluoroethylene)
                .inputItems(TagPrefix.wireGtSingle, GTMaterials.Nichrome, 4)
                .outputItems(new ItemStack(SCBlocks.GAS_CENTRIFUGE_HEATER.get()))
                .EUt(48).duration(200)
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gas_centrifuge_column")
                .inputItems(TagPrefix.pipeNormalFluid, GTMaterials.Steel)
                .inputItems(TagPrefix.pipeTinyFluid, GTMaterials.Steel, 3)
                .outputItems(new ItemStack(SCBlocks.GAS_CENTRIFUGE_COLUMN.get()))
                .EUt(48).duration(200)
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("spent_fuel_casing")
                .inputItems(TagPrefix.rod, SCMaterials.BoronCarbide, 8)
                .outputItems(new ItemStack(SCBlocks.SPENT_FUEL_CASING.get(), 2))
                .EUt(64).duration(200)
                .buildRawRecipe());
    }
}
