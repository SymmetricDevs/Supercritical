package io.github.symmetricdevs.supercritical.common.data

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.registry.GTRegistries
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.MULTIBLOCK
import com.gregtechceu.gtceu.common.data.GTSoundEntries
import io.github.symmetricdevs.supercritical.util.scId
import net.minecraftforge.registries.ForgeRegistries

object ScritRecipeTypes {
    val HEAT_EXCHANGER: GTRecipeType = register("heat_exchanger", MULTIBLOCK)
        .setMaxIOSize(1, 0, 2, 2)
        .setSound(GTSoundEntries.COOLING)

    val SPENT_FUEL_POOL: GTRecipeType = register("spent_fuel_pool", MULTIBLOCK)
        .setMaxIOSize(1, 1, 1, 1)
        .setEUIO(IO.IN)

    val GAS_CENTRIFUGE: GTRecipeType = register("gas_centrifuge", MULTIBLOCK)
        .setMaxIOSize(0, 0, 1, 2)
        .setSound(GTSoundEntries.CENTRIFUGE)
        .setEUIO(IO.IN)


    @Synchronized
    fun init() {
    }

    private fun register(name: String, group: String): GTRecipeType {
        val recipeType = GTRecipeType(scId(name), group)
        ForgeRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType)
        ForgeRegistries.RECIPE_SERIALIZERS.register(recipeType.registryName, GTRecipeSerializer())
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType)
        return recipeType
    }
}