package io.github.symmetricdevs.supercritical.data.recipe.generated

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.common.data.GTMaterials
import io.github.symmetricdevs.supercritical.api.data.chemical.material.property.CoolantProperty
import io.github.symmetricdevs.supercritical.common.data.ScritPropertyKeys
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeTypes
import io.github.symmetricdevs.supercritical.config.ScritConfig
import io.github.symmetricdevs.supercritical.util.outputFluids
import io.github.symmetricdevs.supercritical.util.scId
import net.minecraft.data.recipes.FinishedRecipe
import java.util.function.Consumer
import kotlin.math.ceil

object FluidRecipeHandler {
    fun run(provider: Consumer<FinishedRecipe>, material: Material) {
        if (!material.hasProperty(ScritPropertyKeys.COOLANT)) return
        processCoolant(provider, material, material.getProperty(ScritPropertyKeys.COOLANT))
    }

    fun processCoolant(provider: Consumer<FinishedRecipe>, material: Material, coolant: CoolantProperty) {
        val name = material.name

        var waterAmount = 6
        var coolantAmount = calculateCoolantAmount(material, coolant, waterAmount)
        ScritRecipeTypes.HEAT_EXCHANGER.recipeBuilder(scId("small_${name}_water"))
            .duration(1)
            .circuitMeta(1)
            .inputFluids(coolant.hotCoolant, coolantAmount)
            .inputFluids(GTMaterials.Water, waterAmount)
            .outputFluids(material, coolantAmount)
            .outputFluids(GTMaterials.Steam, Math.toIntExact(waterAmount * 160L))
            .save(provider)
        ScritRecipeTypes.HEAT_EXCHANGER.recipeBuilder(scId("small_${name}_distilled_water"))
            .duration(1)
            .circuitMeta(1)
            .inputFluids(coolant.hotCoolant, coolantAmount)
            .inputFluids(GTMaterials.DistilledWater, waterAmount)
            .outputFluids(material, coolantAmount)
            .outputFluids(GTMaterials.Steam, Math.toIntExact(waterAmount * 160L))
            .save(provider)

        waterAmount = 600
        coolantAmount = calculateCoolantAmount(material, coolant, waterAmount)
        ScritRecipeTypes.HEAT_EXCHANGER.recipeBuilder(scId("large_${name}_water"))
            .duration(1)
            .circuitMeta(2)
            .inputFluids(coolant.hotCoolant, coolantAmount)
            .inputFluids(GTMaterials.Water, waterAmount)
            .outputFluids(material, coolantAmount)
            .outputFluids(GTMaterials.Steam, Math.toIntExact(waterAmount * 160L))
            .save(provider)
        ScritRecipeTypes.HEAT_EXCHANGER.recipeBuilder(scId("large_${name}_distilled_water"))
            .duration(1)
            .circuitMeta(2)
            .inputFluids(coolant.hotCoolant, coolantAmount)
            .inputFluids(GTMaterials.DistilledWater, waterAmount)
            .outputFluids(material, coolantAmount)
            .outputFluids(GTMaterials.Steam, Math.toIntExact(waterAmount * 160L))
            .save(provider)

        ScritRecipeTypes.HEAT_EXCHANGER.recipeBuilder(scId("${name}_radiator"))
            .duration(10)
            .circuitMeta(3)
            .inputFluids(coolant.hotCoolant, 8000)
            .outputFluids(material, 8000)
            .save(provider)
    }

    private fun calculateCoolantAmount(material: Material, coolant: CoolantProperty, waterAmount: Int): Int {
        val multiplier = ScritConfig.INSTANCE.nuclear.heatExchangerEfficiencyMultiplier
        val tHot = coolant.hotCoolantFluid.fluidType.temperature
        val tCold = coolant.coolantFluid.fluidType.temperature
        return ceil(
            100 * 4168 * waterAmount * multiplier
                    / (coolant.specificHeatCapacity * (tHot - tCold))
        ).toInt()
    }
}
