package gregicality.nuclear.loaders.recipe;

import static gregicality.nuclear.api.unification.material.GCYNMaterials.HeavyWater;
import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VH;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.plate;

import gregicality.nuclear.common.blocks.BlockPanelling;
import gregicality.nuclear.common.blocks.GCYNMetaBlocks;

public class GCYNMiscRecipes {

    public static void init() {
        ASSEMBLER_RECIPES.recipeBuilder().EUt(16).duration(120)
                .input(plate, Steel, 4)
                .circuitMeta(16)
                .outputs(GCYNMetaBlocks.PANELLING.getItemVariant(
                        BlockPanelling.PanellingType.GRAY))
                .buildAndRegister();

        for (int i = 0; i < CHEMICAL_DYES.length; i++) {
            CHEMICAL_BATH_RECIPES.recipeBuilder()
                    .inputs(GCYNMetaBlocks.PANELLING.getItemVariant(BlockPanelling.PanellingType.GRAY))
                    .fluidInputs(CHEMICAL_DYES[i].getFluid(9))
                    .outputs(GCYNMetaBlocks.PANELLING.getItemVariant(BlockPanelling.PanellingType.values()[i]))
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
