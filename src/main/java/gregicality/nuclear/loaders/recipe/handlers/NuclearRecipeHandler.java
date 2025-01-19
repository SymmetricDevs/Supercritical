package gregicality.nuclear.loaders.recipe.handlers;

import gregicality.nuclear.api.unification.material.properties.FissionFuelProperty;
import gregicality.nuclear.api.unification.material.properties.GCYNPropertyKey;
import gregicality.nuclear.api.unification.ore.GCYNOrePrefix;
import gregicality.nuclear.common.item.GCYNMetaItems;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;

import static gregicality.nuclear.api.recipes.GCYNRecipeMaps.SPENT_FUEL_POOL_RECIPES;
import static gregicality.nuclear.api.unification.ore.GCYNOrePrefix.*;
import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.CANNER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.FORMING_PRESS_RECIPES;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class NuclearRecipeHandler {

    public static void register() {
        GCYNOrePrefix.fuelRod.addProcessingHandler(GCYNPropertyKey.FISSION_FUEL, NuclearRecipeHandler::processFuelRod);
    }

    private static void processFuelRod(OrePrefix orePrefix, Material material, FissionFuelProperty oreProperty) {
        // This is fine, since it goes up to 320x parallel
        SPENT_FUEL_POOL_RECIPES.recipeBuilder().duration(10000).EUt(20)
                .input(fuelRodHotDepleted, material)
                .output(fuelRodDepleted, material)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(200).EUt(VA[HV])
                .input(fuelRodDepleted, material)
                .output(GCYNMetaItems.FUEL_CLADDING)
                .output(fuelPelletDepleted, material, 16)
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(25).EUt(VA[EV])
                .input(dust, material, 1)
                .notConsumable(MetaItems.SHAPE_MOLD_CYLINDER)
                .output(fuelPellet, material)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(300).EUt(VA[HV])
                .input(fuelPellet, material, 16)
                .input(GCYNMetaItems.FUEL_CLADDING)
                .output(fuelRod, material)
                .buildAndRegister();
    }
}
