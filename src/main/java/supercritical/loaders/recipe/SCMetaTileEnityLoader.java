package supercritical.loaders.recipe;

import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.*;
import static gregtech.loaders.recipe.CraftingComponent.CABLE_QUAD;
import static gregtech.loaders.recipe.CraftingComponent.SENSOR;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.MetaBlocks;
import supercritical.api.unification.material.SCMaterials;
import supercritical.common.SCConfigHolder;
import supercritical.common.blocks.BlockFissionCasing;
import supercritical.common.blocks.SCMetaBlocks;
import supercritical.common.metatileentities.SCMetaTileEntities;

public class SCMetaTileEnityLoader {

    public static void init() {
        ModHandler.addShapedRecipe(true, "fission_reactor", SCMetaTileEntities.FISSION_REACTOR.getStackForm(), "CSC",
                "RHR", "CWC",
                'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.EV),
                'H', SCMetaBlocks.FISSION_CASING.getItemVariant(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL),
                'S', SENSOR.getIngredient(GTValues.EV),
                'R', new UnificationEntry(OrePrefix.rotor, Materials.Steel),
                'W', CABLE_QUAD.getIngredient(GTValues.EV));

        if (SCConfigHolder.misc.enableHX) {
            ModHandler.addShapedRecipe(true, "heat_exchanger", SCMetaTileEntities.HEAT_EXCHANGER.getStackForm(),
                    "FFF",
                    "PCP", "FFF",
                    'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV),
                    'P', new UnificationEntry(OrePrefix.pipeLargeFluid, SCMaterials.Inconel),
                    'F', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID));
        }

        ModHandler.addShapedRecipe(true, "gas_centrifuge", SCMetaTileEntities.GAS_CENTRIFUGE.getStackForm(), "FFF",
                "WRW", "CCC",
                'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.EV),
                'R', new UnificationEntry(OrePrefix.rotor, Materials.Titanium),
                'F', MetaBlocks.METAL_CASING.getItemVariant(PTFE_INERT_CASING),
                'W', CABLE_QUAD.getIngredient(GTValues.EV));

        ModHandler.addShapedRecipe(true, "spent_fuel_pool", SCMetaTileEntities.SPENT_FUEL_POOL.getStackForm(), "PFP",
                "PCP", "PFP",
                'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV),
                'F', MetaBlocks.METAL_CASING.getItemVariant(STAINLESS_CLEAN),
                'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel));
    }
}
