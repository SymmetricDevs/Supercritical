package io.github.symmetricdevs.supercritical.data.recipe

import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import net.minecraft.resources.ResourceLocation
import io.github.symmetricdevs.supercritical.BuildConfig

object ScritRecipeUtils {
    private val DEFERRED_RECIPES: MutableMap<GTRecipeType, MutableMap<ResourceLocation, GTRecipe>> =
        linkedMapOf()

    fun addRecipe(recipeType: GTRecipeType, recipe: GTRecipe) {
        val addonId = ResourceLocation(BuildConfig.MOD_ID, recipe.id.path)
        recipe.id = addonId
        try {
            // Eagerly stage when GTCEu's staging window is open. On success the recipe is already
            // registered, so it must NOT also be deferred — otherwise the mixin replay below would
            // re-add the same id and log "without exact duplicate/conflict".
            recipeType.additionHandler.addStaging(recipe)
        } catch (_: IllegalStateException) {
            // Staging window closed (GTCEu opens it during datapack recipe loading): defer and
            // replay once via addDeferredRecipes(), invoked by MixinGTRecipeType at
            // GTRecipeType.beginStagingRecipes() TAIL during GTCEu's recipe bake.
            DEFERRED_RECIPES.computeIfAbsent(recipeType) { LinkedHashMap() }
                .put(addonId, recipe)
        }
    }

    fun addDeferredRecipes(recipeType: GTRecipeType) {
        val recipes = DEFERRED_RECIPES[recipeType]
        if (recipes == null) return
        for (recipe in recipes.values) {
            recipeType.additionHandler.addStaging(recipe)
        }
    }
}
