package gregicality.nuclear.loaders.recipe.handlers;

import gregicality.nuclear.api.recipes.GCYNRecipeMaps;
import gregicality.nuclear.api.unification.material.properties.CoolantProperty;
import gregicality.nuclear.api.unification.material.properties.GCYNPropertyKey;
import gregicality.nuclear.common.GCYNConfigHolder;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;

public class FluidRecipeHandler {

    public static void runRecipeGeneration() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(GCYNPropertyKey.COOLANT))
                processCoolant(material, material.getProperty(GCYNPropertyKey.COOLANT));
        }
    }

    public static void processCoolant(Material mat, CoolantProperty coolant) {
        int waterAmt = 6;
        double multiplier = GCYNConfigHolder.nuclear.heatExchangerEfficiencyMultiplier;

        // water temp difference * water heat capacity * amount / coolantHeatCapacity * (hotHpTemp - coolantTemp)
        int coolantAmt = (int) Math.ceil(100 * 4168 * waterAmt * multiplier / (coolant.getSpecificHeatCapacity() *
                (coolant.getHotHPCoolant().getFluid().getTemperature() - mat.getFluid().getTemperature())));

        GCYNRecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(1)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(coolantAmt), Materials.Water.getFluid(waterAmt))
                .fluidOutputs(mat.getFluid(coolantAmt), Materials.Steam.getFluid(waterAmt * 160)).buildAndRegister();

        GCYNRecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(1)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(coolantAmt),
                        Materials.DistilledWater.getFluid(waterAmt))
                .fluidOutputs(mat.getFluid(coolantAmt), Materials.Steam.getFluid(waterAmt * 160)).buildAndRegister();
        waterAmt = 600;
        // Slightly more efficient
        coolantAmt = (int) Math.ceil(100 * 4168 * waterAmt * multiplier / (coolant.getSpecificHeatCapacity() *
                (coolant.getHotHPCoolant().getFluid().getTemperature() - mat.getFluid().getTemperature())));;

        GCYNRecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(2)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(coolantAmt), Materials.Water.getFluid(waterAmt))
                .fluidOutputs(mat.getFluid(coolantAmt), Materials.Steam.getFluid(waterAmt * 160)).buildAndRegister();

        GCYNRecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(1).circuitMeta(2)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(coolantAmt),
                        Materials.DistilledWater.getFluid(waterAmt))
                .fluidOutputs(mat.getFluid(coolantAmt), Materials.Steam.getFluid(waterAmt * 160)).buildAndRegister();

        // Radiator
        GCYNRecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder().duration(10).circuitMeta(3)
                .fluidInputs(coolant.getHotHPCoolant().getFluid(8000)).fluidOutputs(mat.getFluid(8000))
                .buildAndRegister();
    }
}
