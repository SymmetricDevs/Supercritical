package gregicality.nuclear.loaders.recipe;

import gregicality.nuclear.common.blocks.BlockFissionCasing;
import gregicality.nuclear.common.blocks.BlockGasCentrifugeCasing;
import gregicality.nuclear.common.blocks.BlockNuclearCasing;
import gregicality.nuclear.common.blocks.GCYNMetaBlocks;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;

import static gregicality.nuclear.api.unification.material.GCYNMaterials.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.Nichrome;
import static gregtech.api.unification.material.Materials.Steel;
import static gregtech.api.unification.ore.OrePrefix.*;

public class GCYNMachineRecipeLoader {

    public static void init() {

        ASSEMBLER_RECIPES.recipeBuilder().EUt(48).duration(280)
                .input(plateDouble, Inconel)
                .input(plate, Steel, 5)
                .input(frameGt, Steel)
                .outputs(GCYNMetaBlocks.FISSION_CASING.getItemVariant(
                        BlockFissionCasing.FissionCasingType.REACTOR_VESSEL, ConfigHolder.recipes.casingsPerCraft))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(48).duration(280)
                .input(pipeLargeFluid, Inconel)
                .input(frameGt, Steel)
                .outputs(GCYNMetaBlocks.FISSION_CASING.getItemVariant(
                        BlockFissionCasing.FissionCasingType.COOLANT_CHANNEL))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(48).duration(280)
                .input(stick, Zircaloy, 6)
                .input(ring, Zircaloy, 1)
                .circuitMeta(1)
                .outputs(GCYNMetaBlocks.FISSION_CASING.getItemVariant(
                        BlockFissionCasing.FissionCasingType.FUEL_CHANNEL))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(48).duration(280)
                .input(stick, Zircaloy, 3)
                .input(ring, Zircaloy, 1)
                .circuitMeta(2)
                .outputs(GCYNMetaBlocks.FISSION_CASING.getItemVariant(
                        BlockFissionCasing.FissionCasingType.CONTROL_ROD_CHANNEL))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(48).duration(200)
                .inputs(MetaBlocks.BOILER_CASING.getItemVariant(
                        BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE))
                .input(wireGtSingle, Nichrome, 4)
                .outputs(GCYNMetaBlocks.NUCLEAR_CASING.getItemVariant(
                        BlockNuclearCasing.NuclearCasingType.GAS_CENTRIFUGE_HEATER))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(48).duration(200)
                .input(pipeNormalFluid, Steel)
                .input(pipeTinyFluid, Steel, 3)
                .outputs(GCYNMetaBlocks.GAS_CENTRIFUGE_CASING.getItemVariant(
                        BlockGasCentrifugeCasing.GasCentrifugeCasingType.GAS_CENTRIFUGE_COLUMN))
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder().EUt(64).duration(200)
                .input(stick, BoronCarbide, 8)
                .outputs(GCYNMetaBlocks.NUCLEAR_CASING.getItemVariant(
                        BlockNuclearCasing.NuclearCasingType.SPENT_FUEL_CASING, ConfigHolder.recipes.casingsPerCraft))
                .buildAndRegister();
    }
}
