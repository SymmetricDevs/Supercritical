package supercritical.loaders.recipe.handlers;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.CANNER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.FORMING_PRESS_RECIPES;
import static gregtech.api.recipes.RecipeMaps.BLAST_RECIPES;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static supercritical.api.recipes.SCRecipeMaps.SPENT_FUEL_POOL_RECIPES;
import static supercritical.api.unification.ore.SCOrePrefix.*;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import supercritical.api.unification.material.properties.FissionFuelProperty;
import supercritical.api.unification.material.properties.SCPropertyKey;
import supercritical.api.unification.ore.SCOrePrefix;
import supercritical.common.item.SCMetaItems;

public class NuclearRecipeHandler {

    public static void register() {
        SCOrePrefix.fuelRod.addProcessingHandler(SCPropertyKey.FISSION_FUEL, NuclearRecipeHandler::processFuelRod);
    }

    private static void processFuelRod(OrePrefix orePrefix, Material material, FissionFuelProperty oreProperty) {
        // This is fine, since it goes up to 320x parallel
        SPENT_FUEL_POOL_RECIPES.recipeBuilder().duration(10000).EUt(20)
                .input(fuelRodHotDepleted, material)
                .output(fuelRodDepleted, material)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(200).EUt(VA[HV])
                .input(fuelRodDepleted, material)
                .output(SCMetaItems.FUEL_CLADDING)
                .output(fuelPelletDepleted, material, 16)
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(25).EUt(VA[EV])
                .input(dust, material, 1)
                .notConsumable(MetaItems.SHAPE_MOLD_CYLINDER)
                .output(fuelPelletRaw, material)
                .buildAndRegister();
                
        BLAST_RECIPES.recipeBuilder().duration(15).EUt(VA[HV]).blastFurnaceTemp(2000)
                .input(fuelPelletRaw, material)
                .output(fuelPellet, material)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(300).EUt(VA[HV])
                .input(fuelPellet, material, 16)
                .input(SCMetaItems.FUEL_CLADDING)
                .output(fuelRod, material)
                .buildAndRegister();
    }
}
