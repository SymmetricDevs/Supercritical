package supercritical.data.recipe.generated

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.common.data.GTMaterials
import supercritical.api.data.chemical.material.property.CoolantProperty
import supercritical.api.data.chemical.material.property.ScritPropertyKey
import supercritical.common.data.ScritRecipeTypes
import supercritical.config.ScritConfig
import supercritical.data.recipe.ScritRecipeUtils
import kotlin.math.ceil

object FluidRecipeHandler {
    fun runRecipeGeneration() {
        ScritRecipeTypes.HEAT_EXCHANGER_RECIPES.beginStagingRecipes()
        for (material in GTCEuAPI.materialManager.registeredMaterials) {
            if (material.hasProperty<CoolantProperty?>(ScritPropertyKey.COOLANT)) {
                processCoolant(material, material.getProperty<CoolantProperty?>(ScritPropertyKey.COOLANT))
            }
        }
        ScritRecipeTypes.HEAT_EXCHANGER_RECIPES.additionHandler.completeStaging()
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

        ScritRecipeUtils.addRecipe(
            ScritRecipeTypes.HEAT_EXCHANGER_RECIPES, ScritRecipeTypes.HEAT_EXCHANGER_RECIPES
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
            if (material.name == materialName && material.hasProperty<CoolantProperty?>(ScritPropertyKey.COOLANT)) {
                coolantMaterial = material
                coolant = material.getProperty<CoolantProperty?>(ScritPropertyKey.COOLANT)
                break
            }
        }
        if (coolantMaterial == null || coolant == null) return

        ScritRecipeUtils.addRecipe(
            ScritRecipeTypes.HEAT_EXCHANGER_RECIPES, ScritRecipeTypes.HEAT_EXCHANGER_RECIPES.recipeBuilder(id)
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
