package supercritical.loaders.recipe;

import supercritical.common.SCConfigHolder;
import supercritical.loaders.recipe.handlers.FluidRecipeHandler;
import supercritical.loaders.recipe.handlers.NuclearRecipeHandler;

public class SCRecipeManager {

    public static void load() {
        if (SCConfigHolder.misc.disableAllRecipes) return;

        SCMiscRecipes.init();
        SCMachineRecipeLoader.init();
        SCMetaTileEnityLoader.init();
        SCMetaTileEntityMachineRecipeLoader.init();
        SCNuclearRecipes.init();
        NuclearRecipeHandler.register();
    }

    public static void loadLatest() {
        SCRecipeModifications.load();
        if (SCConfigHolder.misc.enableHX) {
            FluidRecipeHandler.runRecipeGeneration();
        }
    }
}
