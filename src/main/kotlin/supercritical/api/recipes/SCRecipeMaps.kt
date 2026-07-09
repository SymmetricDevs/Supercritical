package supercritical.api.recipes

import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.GTSoundEntries
import java.util.*

object SCRecipeMaps {
    const val HEAT_EXCHANGER_ID: String = "heat_exchanger"
    const val SPENT_FUEL_POOL_ID: String = "spent_fuel_pool"
    const val GAS_CENTRIFUGE_ID: String = "gas_centrifuge"

    var HEAT_EXCHANGER_RECIPES: GTRecipeType? = null
    var SPENT_FUEL_POOL_RECIPES: GTRecipeType? = null
    var GAS_CENTRIFUGE_RECIPES: GTRecipeType? = null

    private val RECIPE_MAPS: MutableMap<String?, RecipeMapInfo?> = LinkedHashMap<String?, RecipeMapInfo?>()

    private var initialized = false

    @Synchronized
    fun init() {
        if (initialized) return

        HEAT_EXCHANGER_RECIPES = GTRecipeTypes.register(HEAT_EXCHANGER_ID, GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 2, 2)
            .setSound(GTSoundEntries.COOLING)

        SPENT_FUEL_POOL_RECIPES = GTRecipeTypes.register(SPENT_FUEL_POOL_ID, GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 1, 1, 1)

        GAS_CENTRIFUGE_RECIPES = GTRecipeTypes.register(GAS_CENTRIFUGE_ID, GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(0, 0, 1, 2)
            .setSound(GTSoundEntries.CENTRIFUGE)

        register(HEAT_EXCHANGER_ID, HEAT_EXCHANGER_RECIPES, 1, 0, 2, 2)
        register(SPENT_FUEL_POOL_ID, SPENT_FUEL_POOL_RECIPES, 1, 1, 1, 1)
        register(GAS_CENTRIFUGE_ID, GAS_CENTRIFUGE_RECIPES, 0, 0, 1, 2)

        initialized = true
    }

    private fun register(
        id: String?,
        recipeType: GTRecipeType?,
        itemInputs: Int,
        itemOutputs: Int,
        fluidInputs: Int,
        fluidOutputs: Int
    ) {
        RECIPE_MAPS.put(id, RecipeMapInfo(id, recipeType, itemInputs, itemOutputs, fluidInputs, fluidOutputs))
    }

    fun all(): MutableMap<String?, RecipeMapInfo?> {
        return Collections.unmodifiableMap<String?, RecipeMapInfo?>(RECIPE_MAPS)
    }

    @JvmRecord
    data class RecipeMapInfo(
        val id: String?,
        val recipeType: GTRecipeType?,
        val itemInputs: Int,
        val itemOutputs: Int,
        val fluidInputs: Int,
        val fluidOutputs: Int
    )
}
