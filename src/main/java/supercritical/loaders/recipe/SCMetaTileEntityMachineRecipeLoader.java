package supercritical.loaders.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import supercritical.api.unification.material.SCMaterials;
import supercritical.common.registry.SCMachines;

import static supercritical.loaders.recipe.SCRecipeUtils.addRecipe;

public final class SCMetaTileEntityMachineRecipeLoader {

    private SCMetaTileEntityMachineRecipeLoader() {}

    public static void init() {
        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("coolant_input")
                .inputItems(TagPrefix.pipeLargeFluid, SCMaterials.Inconel)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(1)
                .outputItems(SCMachines.COOLANT_INPUT.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV])
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("coolant_output")
                .inputItems(TagPrefix.pipeLargeFluid, SCMaterials.Inconel)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(2)
                .outputItems(SCMachines.COOLANT_OUTPUT.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV])
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fuel_rod_input")
                .inputItems(TagPrefix.rod, SCMaterials.Zircaloy, 6)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(1)
                .outputItems(SCMachines.FUEL_ROD_INPUT.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV])
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fuel_rod_output")
                .inputItems(TagPrefix.rod, SCMaterials.Zircaloy, 6)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(2)
                .outputItems(SCMachines.FUEL_ROD_OUTPUT.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV])
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("control_rod")
                .inputItems(TagPrefix.rodLong, GTMaterials.Hafnium)
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.EV_CIRCUITS)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(1)
                .outputItems(SCMachines.CONTROL_ROD.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV])
                .buildRawRecipe());

        addRecipe(GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("control_rod_moderated")
                .inputItems(TagPrefix.rodLong, GTMaterials.Hafnium)
                .inputItems(TagPrefix.dust, GTMaterials.Graphite)
                .inputItems(com.gregtechceu.gtceu.data.recipe.CustomTags.EV_CIRCUITS)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(2)
                .outputItems(SCMachines.CONTROL_ROD_MODERATED.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV])
                .buildRawRecipe());
    }
}
