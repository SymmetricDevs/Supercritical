package supercritical.common.data

import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.registry.GTRegistries
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gregtechceu.gtceu.common.data.GTSoundEntries
import net.minecraftforge.registries.ForgeRegistries
import supercritical.util.scId

object ScritRecipeTypes {
    const val HEAT_EXCHANGER_ID: String = "heat_exchanger"
    const val SPENT_FUEL_POOL_ID: String = "spent_fuel_pool"
    const val GAS_CENTRIFUGE_ID: String = "gas_centrifuge"

    private var heatExchangerRecipes: GTRecipeType? = null
    private var spentFuelPoolRecipes: GTRecipeType? = null
    private var gasCentrifugeRecipes: GTRecipeType? = null

    val HEAT_EXCHANGER_RECIPES: GTRecipeType
        get() = checkNotNull(heatExchangerRecipes) { "Heat exchanger recipe type has not been initialized" }
    val SPENT_FUEL_POOL_RECIPES: GTRecipeType
        get() = checkNotNull(spentFuelPoolRecipes) { "Spent fuel pool recipe type has not been initialized" }
    val GAS_CENTRIFUGE_RECIPES: GTRecipeType
        get() = checkNotNull(gasCentrifugeRecipes) { "Gas centrifuge recipe type has not been initialized" }

    private var initialized = false

    @Synchronized
    fun init() {
        if (initialized) return

        heatExchangerRecipes = registerRecipeType(HEAT_EXCHANGER_ID, GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 2, 2)
            .setSound(GTSoundEntries.COOLING)

        spentFuelPoolRecipes = registerRecipeType(SPENT_FUEL_POOL_ID, GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 1, 1, 1)

        gasCentrifugeRecipes = registerRecipeType(GAS_CENTRIFUGE_ID, GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(0, 0, 1, 2)
            .setSound(GTSoundEntries.CENTRIFUGE)

        initialized = true
    }

    private fun registerRecipeType(name: String, group: String): GTRecipeType {
        val recipeType = GTRecipeType(scId(name), group)
        // Forge deprecates BuiltInRegistries.RECIPE_TYPE/RECIPE_SERIALIZER ("Use ForgeRegistries
        // instead"). Register directly against the Forge registries — this is exactly what GTCEu's
        // GTRegistries.register(...) helper does internally for these two keys, so behavior is
        // identical (RECIPE_TYPES + RECIPE_SERIALIZERS + GTRegistries.RECIPE_TYPES), without the
        // deprecated accessor.
        ForgeRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType)
        ForgeRegistries.RECIPE_SERIALIZERS.register(recipeType.registryName, GTRecipeSerializer())
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType)
        return recipeType
    }
}