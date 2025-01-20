package gregicality.nuclear.loaders.recipe;

import gregicality.nuclear.loaders.recipe.handlers.FluidRecipeHandler;
import gregicality.nuclear.loaders.recipe.handlers.NuclearRecipeHandler;

public class GCYNRecipeManager {

    public static void load() {
        GCYNMiscRecipes.init();
        GCYNMachineRecipeLoader.init();
        GCYNMetaTileEnityLoader.init();
        GCYNMetaTileEntityMachineRecipeLoader.init();
        GCYNNuclearRecipes.init();
        NuclearRecipeHandler.register();
    }

    public static void loadLatest() {
        GCYNRecipeModifications.load();
        FluidRecipeHandler.runRecipeGeneration();
    }
}
