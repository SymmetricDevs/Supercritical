package gregicality.nuclear.loaders.recipe;

import gregicality.nuclear.common.GCYNConfigHolder;
import gregicality.nuclear.loaders.recipe.handlers.FluidRecipeHandler;
import gregicality.nuclear.loaders.recipe.handlers.NuclearRecipeHandler;

public class GCYNRecipeManager {

    public static void load() {
        if (GCYNConfigHolder.misc.disableAllRecipes) return;

        GCYNMiscRecipes.init();
        GCYNMachineRecipeLoader.init();
        GCYNMetaTileEnityLoader.init();
        GCYNMetaTileEntityMachineRecipeLoader.init();
        GCYNNuclearRecipes.init();
        NuclearRecipeHandler.register();
    }

    public static void loadLatest() {
        GCYNRecipeModifications.load();
        if (GCYNConfigHolder.misc.enableHX) {
            FluidRecipeHandler.runRecipeGeneration();
        }
    }
}
