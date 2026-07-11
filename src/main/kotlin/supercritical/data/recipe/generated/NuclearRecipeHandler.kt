package supercritical.api.recipe.handlers

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import supercritical.api.data.chemical.material.property.FissionFuelProperty
import supercritical.api.data.chemical.material.property.ScritPropertyKey
import supercritical.api.data.chemical.ore.ScritOrePrefix
import supercritical.common.data.ScritItems
import supercritical.common.data.ScritRecipeTypes
import supercritical.data.recipe.ScritRecipeUtils

object NuclearRecipeHandler {
    fun register() {
        // GTCEu Modern no longer exposes the old CEu addProcessingHandler hook. Generate the same recipes eagerly for
        // every material that already carries Supercritical's fission-fuel property.
        for (material in GTCEuAPI.materialManager.registeredMaterials) {
            if (material.hasProperty<FissionFuelProperty?>(ScritPropertyKey.FISSION_FUEL)) {
                processFuelRod(material, material.getProperty<FissionFuelProperty?>(ScritPropertyKey.FISSION_FUEL))
            }
        }
    }

    private fun processFuelRod(material: Material, property: FissionFuelProperty?) {
        val name = material.name
        val fuelItems = ScritItems.NUCLEAR_FUEL_ITEMS[name]

        ScritRecipeUtils.addRecipe(
            ScritRecipeTypes.SPENT_FUEL_POOL_RECIPES, ScritRecipeTypes.SPENT_FUEL_POOL_RECIPES
                .recipeBuilder(name + "_spent_fuel_pool_cooling")
                .duration(10000).EUt(20)
                .inputItems(
                    itemOrPrefix(
                        fuelItems?.hotDepletedFuelRod?.get(),
                        ScritOrePrefix.fuelRodHotDepleted, material
                    )
                )
                .outputItems(
                    itemOrPrefix(
                        fuelItems?.depletedFuelRod?.get(),
                        ScritOrePrefix.fuelRodDepleted, material
                    )
                )
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.CANNER_RECIPES,
            GTRecipeTypes.CANNER_RECIPES.recipeBuilder(name + "_depleted_fuel_rod_unpacking")
                .duration(200).EUt(GTValues.VA[GTValues.HV].toLong())
                .inputItems(
                    itemOrPrefix(
                        fuelItems?.depletedFuelRod?.get(),
                        ScritOrePrefix.fuelRodDepleted, material
                    )
                )
                .outputItems(ScritItems.FUEL_CLADDING.get())
                .outputItems(
                    itemOrPrefix(
                        fuelItems?.depletedFuelPellet?.get(),
                        ScritOrePrefix.fuelPelletDepleted, material, 16
                    )
                )
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.FORMING_PRESS_RECIPES,
            GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(name + "_raw_fuel_pellet")
                .duration(25).EUt(GTValues.VA[GTValues.EV].toLong())
                .inputItems(TagPrefix.dust, material)
                .notConsumable(GTItems.SHAPE_MOLD_CYLINDER)
                .outputItems(
                    itemOrPrefix(
                        fuelItems?.rawFuelPellet?.get(),
                        ScritOrePrefix.fuelPelletRaw, material
                    )
                )
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.BLAST_RECIPES, GTRecipeTypes.BLAST_RECIPES.recipeBuilder(name + "_fuel_pellet")
                .duration(15).EUt(GTValues.VA[GTValues.HV].toLong())
                .blastFurnaceTemp(2000)
                .inputItems(
                    itemOrPrefix(
                        fuelItems?.rawFuelPellet?.get(),
                        ScritOrePrefix.fuelPelletRaw, material
                    )
                )
                .outputItems(
                    itemOrPrefix(
                        fuelItems?.fuelPellet?.get(),
                        ScritOrePrefix.fuelPellet, material
                    )
                )
                .buildRawRecipe()
        )

        ScritRecipeUtils.addRecipe(
            GTRecipeTypes.CANNER_RECIPES, GTRecipeTypes.CANNER_RECIPES.recipeBuilder(name + "_fuel_rod")
                .duration(300).EUt(GTValues.VA[GTValues.HV].toLong())
                .inputItems(
                    itemOrPrefix(
                        fuelItems?.fuelPellet?.get(),
                        ScritOrePrefix.fuelPellet, material, 16
                    )
                )
                .inputItems(ScritItems.FUEL_CLADDING.get())
                .outputItems(
                    itemOrPrefix(
                        fuelItems?.fuelRod?.get(),
                        ScritOrePrefix.fuelRod, material
                    )
                )
                .buildRawRecipe()
        )
    }

    private fun itemOrPrefix(
        item: Item?, prefix: TagPrefix,
        material: Material, count: Int = 1
    ): ItemStack {
        if (item != null) return ItemStack(item, count)
        return ChemicalHelper.get(prefix, material, count)
    }
}
