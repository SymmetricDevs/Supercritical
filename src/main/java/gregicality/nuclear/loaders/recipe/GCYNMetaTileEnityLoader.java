package gregicality.nuclear.loaders.recipe;

import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.*;
import static gregtech.loaders.recipe.CraftingComponent.CABLE_QUAD;
import static gregtech.loaders.recipe.CraftingComponent.SENSOR;

import gregicality.nuclear.api.unification.material.GCYNMaterials;
import gregicality.nuclear.common.GCYNConfigHolder;
import gregicality.nuclear.common.blocks.BlockFissionCasing;
import gregicality.nuclear.common.blocks.GCYNMetaBlocks;
import gregicality.nuclear.common.metatileentities.GCYNMetaTileEntities;
import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.MetaBlocks;

public class GCYNMetaTileEnityLoader {

    public static void init() {
        ModHandler.addShapedRecipe(true, "fission_reactor", GCYNMetaTileEntities.FISSION_REACTOR.getStackForm(), "CSC",
                "RHR", "CWC",
                'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.EV),
                'H', GCYNMetaBlocks.FISSION_CASING.getItemVariant(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL),
                'S', SENSOR.getIngredient(GTValues.EV),
                'R', new UnificationEntry(OrePrefix.rotor, Materials.Steel),
                'W', CABLE_QUAD.getIngredient(GTValues.EV));

        if (GCYNConfigHolder.misc.enableHX) {
            ModHandler.addShapedRecipe(true, "heat_exchanger", GCYNMetaTileEntities.HEAT_EXCHANGER.getStackForm(),
                    "FFF",
                    "PCP", "FFF",
                    'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.HV),
                    'P', new UnificationEntry(OrePrefix.pipeLargeFluid, GCYNMaterials.Inconel),
                    'F', MetaBlocks.METAL_CASING.getItemVariant(STEEL_SOLID));
        }

        ModHandler.addShapedRecipe(true, "gas_centrifuge", GCYNMetaTileEntities.GAS_CENTRIFUGE.getStackForm(), "FFF",
                "WRW", "CCC",
                'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.EV),
                'R', new UnificationEntry(OrePrefix.rotor, Materials.Titanium),
                'F', MetaBlocks.METAL_CASING.getItemVariant(PTFE_INERT_CASING),
                'W', CABLE_QUAD.getIngredient(GTValues.EV));

        ModHandler.addShapedRecipe(true, "spent_fuel_pool", GCYNMetaTileEntities.SPENT_FUEL_POOL.getStackForm(), "PFP",
                "PCP", "PFP",
                'C', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.LV),
                'F', MetaBlocks.METAL_CASING.getItemVariant(STAINLESS_CLEAN),
                'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel));
    }
}
