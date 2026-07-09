package supercritical.loaders.recipe

import supercritical.api.recipe.handlers.FluidRecipeHandler
import supercritical.api.recipe.handlers.NuclearRecipeHandler
import supercritical.api.recipes.SCRecipeMaps
import supercritical.common.SCConfigHolder

object SCRecipeManager {
    var isLoaded: Boolean = false
        private set

    fun load() {
        if (SCConfigHolder.MISC.disableAllRecipes.get() || isLoaded) return

        SCMiscRecipes.init()
        SCMachineRecipeLoader.init()
        SCMetaTileEntityLoader.init()
        SCMetaTileEntityMachineRecipeLoader.init()

        SCRecipeMaps.GAS_CENTRIFUGE_RECIPES.beginStagingRecipes()
        SCRecipeMaps.SPENT_FUEL_POOL_RECIPES.beginStagingRecipes()
        SCNuclearRecipes.init()
        NuclearRecipeHandler.register()
        SCRecipeMaps.GAS_CENTRIFUGE_RECIPES.getAdditionHandler().completeStaging()
        SCRecipeMaps.SPENT_FUEL_POOL_RECIPES.getAdditionHandler().completeStaging()

        isLoaded = true
    }

    fun loadLatest() {
        if (SCConfigHolder.MISC.disableAllRecipes.get()) return
        FluidRecipeHandler.runRecipeGeneration()
    }
}
