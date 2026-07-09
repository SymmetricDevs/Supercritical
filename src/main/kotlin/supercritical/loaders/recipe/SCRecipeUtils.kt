package supercritical.loaders.recipe

import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import net.minecraft.resources.ResourceLocation

object SCRecipeUtils {
    private val DEFERRED_RECIPES: MutableMap<GTRecipeType?, MutableMap<ResourceLocation?, GTRecipe>?> =
        LinkedHashMap<GTRecipeType?, MutableMap<ResourceLocation?, GTRecipe>?>()

    fun addRecipe(recipeType: GTRecipeType, recipe: GTRecipe) {
        DEFERRED_RECIPES.computeIfAbsent(recipeType) { ignoredType: GTRecipeType? -> java.util.LinkedHashMap<net.minecraft.resources.ResourceLocation?, GTRecipe?>() }!!
            .put(recipe.getId(), recipe)
        try {
            recipeType.getAdditionHandler().addStaging(recipe)
        } catch (ignored: IllegalStateException) {
            // GTCEu opens built-in recipe-map staging during datapack recipe loading; replay the saved recipe then.
        }
    }

    fun addDeferredRecipes(recipeType: GTRecipeType) {
        val recipes = DEFERRED_RECIPES.get(recipeType)
        if (recipes == null) return
        for (recipe in recipes.values) {
            recipeType.getAdditionHandler().addStaging(recipe)
        }
    }
}
