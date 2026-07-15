package io.github.symmetricdevs.supercritical.common.data

import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic

typealias RecipeModifier = (MetaMachine, GTRecipe) -> ModifierFunction

object ScritRecipeModifiers {

    inline fun <reified T : MetaMachine> of(
        crossinline block: context(GTRecipe) T.() -> ModifierFunction
    ): RecipeModifier = { machine: MetaMachine, recipe: GTRecipe ->
        (machine as? T)?.let { block(recipe, it) } ?: nullWrongType<T>(machine)
    }

    context(recipe: GTRecipe)
    fun MetaMachine.maxParallel(maxParallel: Int): ModifierFunction {
        if (maxParallel <= 1) return ModifierFunction.IDENTITY

        val parallels = ParallelLogic.getParallelAmountWithoutEU(this, recipe, maxParallel)
        if (parallels <= 1) return ModifierFunction.IDENTITY

        return ModifierFunction.builder()
            .modifyAllContents(ContentModifier.multiplier(parallels.toDouble()))
            .eutMultiplier(parallels.toDouble())
            .parallels(parallels)
            .build()
    }

    inline fun <reified T> nullWrongType(actual: MetaMachine): ModifierFunction {
        return com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier.nullWrongType(T::class.java, actual)
    }
}