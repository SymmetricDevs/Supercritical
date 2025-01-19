package gregicality.nuclear.loaders.recipe;

import gregtech.api.unification.material.MarkerMaterials;

import static gregicality.nuclear.api.unification.material.GCYNMaterials.Inconel;
import static gregicality.nuclear.api.unification.material.GCYNMaterials.Zircaloy;
import static gregicality.nuclear.common.metatileentities.GCYNMetaTileEntities.*;
import static gregtech.api.GTValues.EV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.metatileentities.MetaTileEntities.HULL;

public class GCYNMetaTileEntityMachineRecipeLoader {

    public static void init() {
        // Nuclear Technology

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(pipeLargeFluid, Inconel)
                .input(HULL[EV])
                .fluidInputs(Polyethylene.getFluid(144))
                .circuitMeta(1)
                .outputs(COOLANT_INPUT.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(pipeLargeFluid, Inconel)
                .input(HULL[EV])
                .fluidInputs(Polyethylene.getFluid(144))
                .circuitMeta(2)
                .outputs(COOLANT_OUTPUT.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stick, Zircaloy, 6)
                .input(HULL[EV])
                .fluidInputs(Polyethylene.getFluid(144))
                .circuitMeta(1)
                .outputs(FUEL_ROD_INPUT.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stick, Zircaloy, 6)
                .input(HULL[EV])
                .fluidInputs(Polyethylene.getFluid(144))
                .circuitMeta(2)
                .outputs(FUEL_ROD_OUTPUT.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stickLong, Hafnium)
                .input(circuit, MarkerMaterials.Tier.EV)
                .input(HULL[EV])
                .circuitMeta(1)
                .fluidInputs(Polyethylene.getFluid(144))
                .outputs(CONTROL_ROD.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(stickLong, Hafnium)
                .input(dust, Graphite)
                .input(circuit, MarkerMaterials.Tier.EV)
                .input(HULL[EV])
                .circuitMeta(2)
                .fluidInputs(Polyethylene.getFluid(144))
                .outputs(CONTROL_ROD_MODERATED.getStackForm())
                .duration(300).EUt(VA[EV]).buildAndRegister();
    }
}
