package supercritical.loaders.recipe

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.data.recipe.CustomTags
import supercritical.api.unification.material.ScritMaterials
import supercritical.common.registry.ScritMachines

object ScritMetaTileEntityMachineRecipeLoader {
    fun init() {
        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("coolant_input")
                .inputItems(TagPrefix.pipeLargeFluid, ScritMaterials.Inconel)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(1)
                .outputItems(ScritMachines.COOLANT_INPUT.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("coolant_output")
                .inputItems(TagPrefix.pipeLargeFluid, ScritMaterials.Inconel)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(2)
                .outputItems(ScritMachines.COOLANT_OUTPUT.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fuel_rod_input")
                .inputItems(TagPrefix.rod, ScritMaterials.Zircaloy, 6)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(1)
                .outputItems(ScritMachines.FUEL_ROD_INPUT.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fuel_rod_output")
                .inputItems(TagPrefix.rod, ScritMaterials.Zircaloy, 6)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(2)
                .outputItems(ScritMachines.FUEL_ROD_OUTPUT.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("control_rod")
                .inputItems(TagPrefix.rodLong, GTMaterials.Hafnium)
                .inputItems(CustomTags.EV_CIRCUITS)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(1)
                .outputItems(ScritMachines.CONTROL_ROD.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("control_rod_moderated")
                .inputItems(TagPrefix.rodLong, GTMaterials.Hafnium)
                .inputItems(TagPrefix.dust, GTMaterials.Graphite)
                .inputItems(CustomTags.EV_CIRCUITS)
                .inputItems(GTMachines.HULL[GTValues.EV].asStack())
                .inputFluids(GTMaterials.Polyethylene.getFluid(144))
                .circuitMeta(2)
                .outputItems(ScritMachines.CONTROL_ROD_MODERATED.asStack())
                .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )
    }
}
