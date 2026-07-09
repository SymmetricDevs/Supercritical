package supercritical.loaders.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import supercritical.api.unification.material.SCMaterials;
import supercritical.common.registry.SCBlocks;
import supercritical.common.registry.SCMachines;

import net.minecraft.world.item.ItemStack;

import static supercritical.loaders.recipe.SCRecipeUtils.addRecipe;

public final class SCMetaTileEntityLoader {

    private SCMetaTileEntityLoader() {}

    public static void init() {
        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fission_reactor")
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.EV_CIRCUITS)
                .inputItems(new ItemStack(SCBlocks.REACTOR_VESSEL.get()))
                .inputItems(GTItems.SENSOR_EV.asStack())
                .inputItems(TagPrefix.rotor, GTMaterials.Steel, 2)
                .inputItems(TagPrefix.wireGtQuadruple, GTMaterials.AnnealedCopper)
                .outputItems(SCMachines.FISSION_REACTOR.asStack())
                .duration(400).EUt(GTValues.VA[GTValues.EV])
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("heat_exchanger")
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.HV_CIRCUITS)
                .inputItems(TagPrefix.pipeLargeFluid, SCMaterials.Inconel, 2)
                .inputItems(new ItemStack(GTBlocks.CASING_STEEL_SOLID.get(), 6))
                .outputItems(SCMachines.HEAT_EXCHANGER.asStack())
                .duration(400).EUt(GTValues.VA[GTValues.HV])
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("gas_centrifuge")
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.EV_CIRCUITS)
                .inputItems(TagPrefix.rotor, GTMaterials.Titanium)
                .inputItems(new ItemStack(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get(), 3))
                .inputItems(TagPrefix.wireGtQuadruple, GTMaterials.AnnealedCopper, 2)
                .outputItems(SCMachines.GAS_CENTRIFUGE.asStack())
                .duration(400).EUt(GTValues.VA[GTValues.EV])
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("spent_fuel_pool")
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.LV_CIRCUITS)
                .inputItems(new ItemStack(SCBlocks.SPENT_FUEL_CASING.get(), 2))
                .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 4)
                .outputItems(SCMachines.SPENT_FUEL_POOL.asStack())
                .duration(400).EUt(GTValues.VA[GTValues.LV])
                .buildRawRecipe());
    }
}
