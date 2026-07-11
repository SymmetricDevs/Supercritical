package supercritical.loaders.recipe

import supercritical.api.recipe.handlers.FluidRecipeHandler
import supercritical.api.recipe.handlers.NuclearRecipeHandler
import supercritical.common.SCConfigHolder

object SCRecipeManager {
    var isLoaded: Boolean = false
        private set

    fun load() {
        if (SCConfigHolder.INSTANCE.misc.disableAllRecipes ||
            SCConfigHolder.INSTANCE.misc.disableAllMaterials || isLoaded
        ) return

        SCMiscRecipes.init()
        SCMachineRecipeLoader.init()
        SCMetaTileEntityLoader.init()
        SCMetaTileEntityMachineRecipeLoader.init()

        // Recipes are staged through SCRecipeUtils.addRecipe, which defers them when GTCEu's
        // staging window is closed and replays them via MixinGTRecipeType. GTCEu's own
        // RecipeManagerMixin bakes each recipe type during datapack reload, after its built-in
        // MapIngredientFunctions are registered in common setup. Do not bake staging manually
        // here: doing so races GTCEu's MapIngredientTypeManager registration and throws an NPE
        // in StagingRecipeDB.populateDB -> MapIngredientTypeManager.getFrom.
        SCNuclearRecipes.init()
        NuclearRecipeHandler.register()

        isLoaded = true
    }

    fun loadLatest() {
        if (SCConfigHolder.INSTANCE.misc.disableAllRecipes ||
            SCConfigHolder.INSTANCE.misc.disableAllMaterials
        ) return
        FluidRecipeHandler.runRecipeGeneration()
    }
}
