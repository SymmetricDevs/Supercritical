package supercritical.loaders.recipe;

import supercritical.api.recipe.handlers.FluidRecipeHandler;
import supercritical.api.recipe.handlers.NuclearRecipeHandler;
import supercritical.api.recipes.SCRecipeMaps;
import supercritical.common.SCConfigHolder;

public final class SCRecipeManager {

    private static boolean loaded;

    private SCRecipeManager() {}

    public static void load() {
        if (SCConfigHolder.MISC.disableAllRecipes.get() || loaded) return;

        SCMiscRecipes.init();
        SCMachineRecipeLoader.init();
        SCMetaTileEntityLoader.init();
        SCMetaTileEntityMachineRecipeLoader.init();

        SCRecipeMaps.GAS_CENTRIFUGE_RECIPES.beginStagingRecipes();
        SCRecipeMaps.SPENT_FUEL_POOL_RECIPES.beginStagingRecipes();
        SCNuclearRecipes.init();
        NuclearRecipeHandler.register();
        SCRecipeMaps.GAS_CENTRIFUGE_RECIPES.getAdditionHandler().completeStaging();
        SCRecipeMaps.SPENT_FUEL_POOL_RECIPES.getAdditionHandler().completeStaging();

        loaded = true;
    }

    public static void loadLatest() {
        if (SCConfigHolder.MISC.disableAllRecipes.get()) return;
        FluidRecipeHandler.runRecipeGeneration();
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
