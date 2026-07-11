package supercritical.api.recipe.handlers

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.common.data.GTMaterials
import supercritical.api.recipes.SCRecipeMaps
import supercritical.api.unification.material.properties.CoolantProperty
import supercritical.api.unification.material.properties.SCPropertyKey
import supercritical.common.ScritConfig
import supercritical.loaders.recipe.SCRecipeUtils
import kotlin.math.ceil

object FluidRecipeHandler {
    fun runRecipeGeneration() {
        SCRecipeMaps.HEAT_EXCHANGER_RECIPES.beginStagingRecipes()
        for (material in GTCEuAPI.materialManager.registeredMaterials) {
            if (material.hasProperty<CoolantProperty?>(SCPropertyKey.COOLANT)) {
                processCoolant(material, material.getProperty<CoolantProperty?>(SCPropertyKey.COOLANT))
            }
        }
        SCRecipeMaps.HEAT_EXCHANGER_RECIPES.additionHandler.completeStaging()
    }

    fun processCoolant(material: Material, coolant: CoolantProperty) {
        var waterAmount = 6
        var coolantAmount = calculateCoolantAmount(material, coolant, waterAmount)
        addHeatExchangerRecipe("small_" + material.name + "_water", 1, waterAmount, coolantAmount, false)
        addHeatExchangerRecipe("small_" + material.name + "_distilled_water", 1, waterAmount, coolantAmount, true)

        waterAmount = 600
        coolantAmount = calculateCoolantAmount(material, coolant, waterAmount)
        addHeatExchangerRecipe("large_" + material.name + "_water", 2, waterAmount, coolantAmount, false)
        addHeatExchangerRecipe("large_" + material.name + "_distilled_water", 2, waterAmount, coolantAmount, true)

        SCRecipeUtils.addRecipe(
            SCRecipeMaps.HEAT_EXCHANGER_RECIPES, SCRecipeMaps.HEAT_EXCHANGER_RECIPES
                .recipeBuilder(material.name + "_radiator")
                .duration(10)
                .circuitMeta(3)
                .inputFluids(coolant.hotHPCoolant.getFluid(8000))
                .outputFluids(material.getFluid(8000))
                .buildRawRecipe()
        )
    }

    private fun addHeatExchangerRecipe(
        id: String, circuit: Int, waterAmount: Int, coolantAmount: Int,
        distilled: Boolean
    ) {
        val water = if (distilled) GTMaterials.DistilledWater else GTMaterials.Water
        // Re-resolve from the recipe id prefix because GTCEu's builder does not carry external closure state.
        var coolantMaterial: Material? = null
        var coolant: CoolantProperty? = null
        val materialName =
            id.substring(id.indexOf('_') + 1, id.lastIndexOf(if (distilled) "_distilled_water" else "_water"))
        for (material in GTCEuAPI.materialManager.registeredMaterials) {
            if (material.name == materialName && material.hasProperty<CoolantProperty?>(SCPropertyKey.COOLANT)) {
                coolantMaterial = material
                coolant = material.getProperty<CoolantProperty?>(SCPropertyKey.COOLANT)
                break
            }
        }
        if (coolantMaterial == null || coolant == null) return

        SCRecipeUtils.addRecipe(
            SCRecipeMaps.HEAT_EXCHANGER_RECIPES, SCRecipeMaps.HEAT_EXCHANGER_RECIPES.recipeBuilder(id)
                .duration(1)
                .circuitMeta(circuit)
                .inputFluids(coolant.hotHPCoolant.getFluid(coolantAmount), water.getFluid(waterAmount))
                .outputFluids(
                    coolantMaterial.getFluid(coolantAmount),
                    GTMaterials.Steam.getFluid(Math.toIntExact(waterAmount * 160L))
                )
                .buildRawRecipe()
        )
    }

    private fun calculateCoolantAmount(material: Material, coolant: CoolantProperty, waterAmount: Int): Int {
        val multiplier = ScritConfig.INSTANCE.nuclear.heatExchangerEfficiencyMultiplier
        val hotTemperature = coolant.hotHPCoolant.fluid.getFluidType().temperature
        val coldTemperature = material.fluid.getFluidType().temperature
        return ceil(
            100 * 4168 * waterAmount * multiplier /
                    (coolant.specificHeatCapacity * (hotTemperature - coldTemperature))
        ).toInt()
    }
}
