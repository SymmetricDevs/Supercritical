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
import supercritical.api.recipes.SCRecipeMaps
import supercritical.api.unification.material.properties.FissionFuelProperty
import supercritical.api.unification.material.properties.SCPropertyKey
import supercritical.api.unification.ore.SCOrePrefix
import supercritical.common.registry.SCItems
import supercritical.loaders.recipe.SCRecipeUtils

object NuclearRecipeHandler {
    fun register() {
        // GTCEu Modern no longer exposes the old CEu addProcessingHandler hook. Generate the same recipes eagerly for
        // every material that already carries Supercritical's fission-fuel property.
        for (material in GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty<FissionFuelProperty?>(SCPropertyKey.FISSION_FUEL)) {
                processFuelRod(material, material.getProperty<FissionFuelProperty?>(SCPropertyKey.FISSION_FUEL))
            }
        }
    }

    private fun processFuelRod(material: Material, property: FissionFuelProperty?) {
        val name = material.getName()
        val fuelItems = SCItems.NUCLEAR_FUEL_ITEMS.get(name)

        SCRecipeUtils.addRecipe(
            SCRecipeMaps.SPENT_FUEL_POOL_RECIPES, SCRecipeMaps.SPENT_FUEL_POOL_RECIPES
                .recipeBuilder(name + "_spent_fuel_pool_cooling")
                .duration(10000).EUt(20)
                .inputItems(
                    itemOrPrefix(
                        if (fuelItems == null) null else fuelItems.hotDepletedFuelRod.get(),
                        SCOrePrefix.fuelRodHotDepleted, material
                    )
                )
                .outputItems(
                    itemOrPrefix(
                        if (fuelItems == null) null else fuelItems.depletedFuelRod.get(),
                        SCOrePrefix.fuelRodDepleted, material
                    )
                )
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.CANNER_RECIPES,
            GTRecipeTypes.CANNER_RECIPES.recipeBuilder(name + "_depleted_fuel_rod_unpacking")
                .duration(200).EUt(GTValues.VA[GTValues.HV].toLong())
                .inputItems(
                    itemOrPrefix(
                        if (fuelItems == null) null else fuelItems.depletedFuelRod.get(),
                        SCOrePrefix.fuelRodDepleted, material
                    )
                )
                .outputItems(SCItems.FUEL_CLADDING.get())
                .outputItems(
                    itemOrPrefix(
                        if (fuelItems == null) null else fuelItems.depletedFuelPellet.get(),
                        SCOrePrefix.fuelPelletDepleted, material, 16
                    )
                )
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.FORMING_PRESS_RECIPES,
            GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(name + "_raw_fuel_pellet")
                .duration(25).EUt(GTValues.VA[GTValues.EV].toLong())
                .inputItems(TagPrefix.dust, material)
                .notConsumable(GTItems.SHAPE_MOLD_CYLINDER)
                .outputItems(
                    itemOrPrefix(
                        if (fuelItems == null) null else fuelItems.rawFuelPellet.get(),
                        SCOrePrefix.fuelPelletRaw, material
                    )
                )
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.BLAST_RECIPES, GTRecipeTypes.BLAST_RECIPES.recipeBuilder(name + "_fuel_pellet")
                .duration(15).EUt(GTValues.VA[GTValues.HV].toLong())
                .blastFurnaceTemp(2000)
                .inputItems(
                    itemOrPrefix(
                        if (fuelItems == null) null else fuelItems.rawFuelPellet.get(),
                        SCOrePrefix.fuelPelletRaw, material
                    )
                )
                .outputItems(
                    itemOrPrefix(
                        if (fuelItems == null) null else fuelItems.fuelPellet.get(),
                        SCOrePrefix.fuelPellet, material
                    )
                )
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.CANNER_RECIPES, GTRecipeTypes.CANNER_RECIPES.recipeBuilder(name + "_fuel_rod")
                .duration(300).EUt(GTValues.VA[GTValues.HV].toLong())
                .inputItems(
                    itemOrPrefix(
                        if (fuelItems == null) null else fuelItems.fuelPellet.get(),
                        SCOrePrefix.fuelPellet, material, 16
                    )
                )
                .inputItems(SCItems.FUEL_CLADDING.get())
                .outputItems(
                    itemOrPrefix(
                        if (fuelItems == null) null else fuelItems.fuelRod.get(),
                        SCOrePrefix.fuelRod, material
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
