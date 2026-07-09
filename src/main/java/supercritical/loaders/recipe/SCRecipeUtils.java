package supercritical.loaders.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SCRecipeUtils {

    private static final Map<GTRecipeType, Map<ResourceLocation, GTRecipe>> DEFERRED_RECIPES = new LinkedHashMap<>();

    private SCRecipeUtils() {}

    public static void addRecipe(GTRecipeType recipeType, GTRecipe recipe) {
        DEFERRED_RECIPES.computeIfAbsent(recipeType, ignoredType -> new LinkedHashMap<>()).put(recipe.getId(), recipe);
        try {
            recipeType.getAdditionHandler().addStaging(recipe);
        } catch (IllegalStateException ignored) {
            // GTCEu opens built-in recipe-map staging during datapack recipe loading; replay the saved recipe then.
        }
    }

    public static void addDeferredRecipes(GTRecipeType recipeType) {
        Map<ResourceLocation, GTRecipe> recipes = DEFERRED_RECIPES.get(recipeType);
        if (recipes == null) return;
        for (GTRecipe recipe : recipes.values()) {
            recipeType.getAdditionHandler().addStaging(recipe);
        }
    }
}
