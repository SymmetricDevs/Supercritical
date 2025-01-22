package supercritical.loaders.recipe;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VH;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.plate;
import static supercritical.api.unification.material.SCMaterials.HeavyWater;

import supercritical.common.blocks.BlockPanelling;
import supercritical.common.blocks.SCMetaBlocks;

public class SCMiscRecipes {

    public static void init() {
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).duration(120)
                .input(plate, Steel, 4)
                .circuitMeta(16)
                .outputs(SCMetaBlocks.PANELLING.getItemVariant(
                        BlockPanelling.PanellingType.GRAY))
                .buildAndRegister();

        for (int i = 0; i < CHEMICAL_DYES.length; i++) {
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(SCMetaBlocks.PANELLING.getItemVariant(BlockPanelling.PanellingType.GRAY))
                    .fluidInputs(CHEMICAL_DYES[i].getFluid(9))
                    .outputs(SCMetaBlocks.PANELLING.getItemVariant(BlockPanelling.PanellingType.values()[i]))
                    .EUt(2).duration(10)
                    .buildAndRegister();
        }

        CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Deuterium.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(HeavyWater.getFluid(1000))
                .duration(200).EUt(VH[LV]).buildAndRegister();
    }
}
