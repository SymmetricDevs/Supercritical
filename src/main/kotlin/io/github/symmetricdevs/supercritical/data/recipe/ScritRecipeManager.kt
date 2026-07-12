package io.github.symmetricdevs.supercritical.data.recipe

import io.github.symmetricdevs.supercritical.data.recipe.generated.NuclearRecipeHandler
import io.github.symmetricdevs.supercritical.config.ScritConfig
import io.github.symmetricdevs.supercritical.data.recipe.generated.FluidRecipeHandler

object ScritRecipeManager {
    var isLoaded: Boolean = false
        private set

    fun load() {
        if (ScritConfig.INSTANCE.misc.disableAllRecipes ||
            ScritConfig.INSTANCE.misc.disableAllMaterials || isLoaded
        ) return

        ScritMiscRecipes.init()
        ScritMachineRecipeLoader.init()
        ScritMetaTileEntityLoader.init()
        ScritMetaTileEntityMachineRecipeLoader.init()

        // Recipes are staged through SCRecipeUtils.addRecipe, which defers them when GTCEu's
        // staging window is closed and replays them via MixinGTRecipeType. GTCEu's own
        // RecipeManagerMixin bakes each recipe type during datapack reload, after its built-in
        // MapIngredientFunctions are registered in common setup. Do not bake staging manually
        // here: doing so races GTCEu's MapIngredientTypeManager registration and throws an NPE
        // in StagingRecipeDB.populateDB -> MapIngredientTypeManager.getFrom.
        ScritNuclearRecipes.init()
        NuclearRecipeHandler.register()

        isLoaded = true
    }

    fun loadLatest() {
        if (ScritConfig.INSTANCE.misc.disableAllRecipes ||
            ScritConfig.INSTANCE.misc.disableAllMaterials
        ) return
        FluidRecipeHandler.runRecipeGeneration()
    }
}
