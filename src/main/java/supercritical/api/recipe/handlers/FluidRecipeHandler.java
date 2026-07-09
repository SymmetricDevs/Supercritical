package supercritical.api.recipe.handlers;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import supercritical.api.recipes.SCRecipeMaps;
import supercritical.api.unification.material.properties.CoolantProperty;
import supercritical.api.unification.material.properties.SCPropertyKey;
import supercritical.common.SCConfigHolder;

import static supercritical.loaders.recipe.SCRecipeUtils.addRecipe;

public final class FluidRecipeHandler {

    private FluidRecipeHandler() {}

    public static void runRecipeGeneration() {
        if (!SCConfigHolder.MISC.enableHX.get()) return;
        SCRecipeMaps.HEAT_EXCHANGER_RECIPES.beginStagingRecipes();
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(SCPropertyKey.COOLANT)) {
                processCoolant(material, material.getProperty(SCPropertyKey.COOLANT));
            }
        }
        SCRecipeMaps.HEAT_EXCHANGER_RECIPES.getAdditionHandler().completeStaging();
    }

    public static void processCoolant(Material material, CoolantProperty coolant) {
        int waterAmount = 6;
        int coolantAmount = calculateCoolantAmount(material, coolant, waterAmount);
        addHeatExchangerRecipe("small_" + material.getName() + "_water", 1, waterAmount, coolantAmount, false);
        addHeatExchangerRecipe("small_" + material.getName() + "_distilled_water", 1, waterAmount, coolantAmount, true);

        waterAmount = 600;
        coolantAmount = calculateCoolantAmount(material, coolant, waterAmount);
        addHeatExchangerRecipe("large_" + material.getName() + "_water", 2, waterAmount, coolantAmount, false);
        addHeatExchangerRecipe("large_" + material.getName() + "_distilled_water", 2, waterAmount, coolantAmount, true);

        addRecipe(SCRecipeMaps.HEAT_EXCHANGER_RECIPES, SCRecipeMaps.HEAT_EXCHANGER_RECIPES
                .recipeBuilder(material.getName() + "_radiator")
                .duration(10)
                .circuitMeta(3)
                .inputFluids(coolant.getHotHPCoolant().getFluid(8000))
                .outputFluids(material.getFluid(8000))
                .buildRawRecipe());
    }

    private static void addHeatExchangerRecipe(String id, int circuit, int waterAmount, int coolantAmount,
                                               boolean distilled) {
        Material water = distilled ? GTMaterials.DistilledWater : GTMaterials.Water;
        // Re-resolve from the recipe id prefix because GTCEu's builder does not carry external closure state.
        Material coolantMaterial = null;
        CoolantProperty coolant = null;
        String materialName = id.substring(id.indexOf('_') + 1, id.lastIndexOf(distilled ? "_distilled_water" : "_water"));
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.getName().equals(materialName) && material.hasProperty(SCPropertyKey.COOLANT)) {
                coolantMaterial = material;
                coolant = material.getProperty(SCPropertyKey.COOLANT);
                break;
            }
        }
        if (coolantMaterial == null || coolant == null) return;

        addRecipe(SCRecipeMaps.HEAT_EXCHANGER_RECIPES, SCRecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder(id)
                .duration(1)
                .circuitMeta(circuit)
                .inputFluids(coolant.getHotHPCoolant().getFluid(coolantAmount), water.getFluid(waterAmount))
                .outputFluids(coolantMaterial.getFluid(coolantAmount), GTMaterials.Steam.getFluid(Math.toIntExact(waterAmount * 160L)))
                .buildRawRecipe());
    }

    private static int calculateCoolantAmount(Material material, CoolantProperty coolant, int waterAmount) {
        double multiplier = SCConfigHolder.NUCLEAR.heatExchangerEfficiencyMultiplier.get();
        int hotTemperature = coolant.getHotHPCoolant().getFluid().getFluidType().getTemperature();
        int coldTemperature = material.getFluid().getFluidType().getTemperature();
        return (int) Math.ceil(100 * 4168 * waterAmount * multiplier /
                (coolant.getSpecificHeatCapacity() * (hotTemperature - coldTemperature)));
    }
}
