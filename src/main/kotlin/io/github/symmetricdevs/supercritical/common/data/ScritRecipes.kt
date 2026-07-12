package io.github.symmetricdevs.supercritical.common.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import io.github.symmetricdevs.supercritical.data.recipe.MachineRecipes
import io.github.symmetricdevs.supercritical.data.recipe.MiscRecipes
import io.github.symmetricdevs.supercritical.data.recipe.NuclearRecipes
import io.github.symmetricdevs.supercritical.data.recipe.generated.FluidRecipeHandler
import io.github.symmetricdevs.supercritical.data.recipe.generated.NuclearRecipeHandler
import net.minecraft.data.recipes.FinishedRecipe
import java.util.function.Consumer

object ScritRecipes {

    fun init(provider: Consumer<FinishedRecipe>) {
        MachineRecipes.init(provider)
        MiscRecipes.init(provider)
        NuclearRecipes.init(provider)

        // Per-material generated recipes, mirroring GTCEu's GTRecipes#recipeAddition loop: iterate
        // every registered material once and let each handler decide via its own property check
        // whether to emit recipes. The COOLANT / FISSION_FUEL properties are attached during GTCEu's
        // MaterialEvent / PostMaterialEvent, which fire before datagen, so they are already set here.
        for (material in GTCEuAPI.materialManager.registeredMaterials) {
            FluidRecipeHandler.run(provider, material)
            NuclearRecipeHandler.run(provider, material)
        }
    }
}
