package io.github.symmetricdevs.supercritical.data.recipe.generated

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import io.github.symmetricdevs.supercritical.api.data.chemical.material.property.FissionFuelProperty
import io.github.symmetricdevs.supercritical.api.data.chemical.material.property.ScritPropertyKey
import io.github.symmetricdevs.supercritical.common.data.ScritItems
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeTypes
import io.github.symmetricdevs.supercritical.common.data.ScritTagPrefixes
import io.github.symmetricdevs.supercritical.util.scId
import net.minecraft.data.recipes.FinishedRecipe
import java.util.function.Consumer

object NuclearRecipeHandler {
    // GTCEu Modern no longer exposes the old CEu addProcessingHandler hook. Generate the same recipes per
    // material, mirroring GTCEu's generated/*RecipeHandler.run(provider, material) idiom — the material
    // loop lives in ScritRecipes. FissionFuelProperty is attached during GTCEu's MaterialEvent, which
    // fires before datagen, so it is already set when this runs.
    fun run(provider: Consumer<FinishedRecipe>, material: Material) {
        if (!material.hasProperty(ScritPropertyKey.FISSION_FUEL)) return
        processFuelRod(provider, material, material.getProperty<FissionFuelProperty>(ScritPropertyKey.FISSION_FUEL))
    }

    private fun processFuelRod(provider: Consumer<FinishedRecipe>, material: Material, property: FissionFuelProperty) {
        val name = material.name

        ScritRecipeTypes.SPENT_FUEL_POOL_RECIPES
            .recipeBuilder(scId("${name}_spent_fuel_pool_cooling"))
            .inputItems(ScritTagPrefixes.fuelRodHotDepleted, material)
            .outputItems(ScritTagPrefixes.fuelRodDepleted, material)
            .duration(10000).EUt(20)
            .save(provider)

        GTRecipeTypes.CANNER_RECIPES.recipeBuilder(scId("${name}_depleted_fuel_rod_unpacking"))
            .duration(200).EUt(GTValues.VA[GTValues.HV].toLong())
            .inputItems(ScritTagPrefixes.fuelRodDepleted, material)
            .outputItems(ScritItems.FUEL_CLADDING)
            .outputItems(ScritTagPrefixes.fuelPelletDepleted, material, 16)
            .save(provider)

        GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(scId("${name}_raw_fuel_pellet"))
            .duration(25).EUt(GTValues.VA[GTValues.EV].toLong())
            .inputItems(TagPrefix.dust, material)
            .notConsumable(GTItems.SHAPE_MOLD_CYLINDER)
            .outputItems(ScritTagPrefixes.fuelPelletRaw, material)
            .save(provider)

        GTRecipeTypes.BLAST_RECIPES.recipeBuilder(scId("${name}_fuel_pellet"))
            .duration(15).EUt(GTValues.VA[GTValues.HV].toLong())
            .blastFurnaceTemp(2000)
            .inputItems(ScritTagPrefixes.fuelPelletRaw, material)
            .outputItems(ScritTagPrefixes.fuelPellet, material)
            .save(provider)

        GTRecipeTypes.CANNER_RECIPES.recipeBuilder(scId("${name}_fuel_rod"))
            .duration(300).EUt(GTValues.VA[GTValues.HV].toLong())
            .inputItems(ScritTagPrefixes.fuelPellet, material, 16)
            .inputItems(ScritItems.FUEL_CLADDING)
            .outputItems(ScritTagPrefixes.fuelRod, material)
            .save(provider)
    }
}
