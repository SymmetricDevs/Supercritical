package gregicality.nuclear.loaders.recipe;

import gregicality.nuclear.common.blocks.BlockPanelling;
import gregicality.nuclear.common.blocks.GCYNMetaBlocks;

import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_BATH_RECIPES;
import static gregtech.api.unification.material.Materials.CHEMICAL_DYES;
import static gregtech.api.unification.material.Materials.Steel;
import static gregtech.api.unification.ore.OrePrefix.plate;

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
    }
}
