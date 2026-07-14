package io.github.symmetricdevs.supercritical.integration.jei.basic

import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionFuelRegistry
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.xei.FissionFuelInfo
import io.github.symmetricdevs.supercritical.integration.xei.widgets.FissionFuelInfoWidget
import io.github.symmetricdevs.supercritical.util.scId
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.helpers.IJeiHelpers
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.recipe.category.IRecipeCategory
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class FissionFuelCategory(helpers: IJeiHelpers) : IRecipeCategory<FissionFuelInfo> {
    private val icon: IDrawable =
        helpers.guiHelper.createDrawableItemStack(ScritMachines.FISSION_REACTOR.asStack())

    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: FissionFuelInfo, focuses: IFocusGroup) {
        builder.addSlot(
            RecipeIngredientRole.INPUT,
            FissionFuelInfoWidget.INGREDIENT_INPUT_X, FissionFuelInfoWidget.INGREDIENT_INPUT_Y
        ).addItemStack(recipe.rod)
        builder.addSlot(
            RecipeIngredientRole.OUTPUT,
            FissionFuelInfoWidget.INGREDIENT_OUTPUT_X, FissionFuelInfoWidget.INGREDIENT_OUTPUT_Y
        ).addItemStacks(recipe.depletedRods)
    }

    override fun draw(
        recipe: FissionFuelInfo, recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double
    ) {
        // buildSlots = false: JEI registers its own native slots on top, so the shared widget
        // only contributes the slot backgrounds + arrow + stat text (no double-drawn content).
        FissionFuelInfoWidget(recipe, buildSlots = false)
            .drawInBackground(guiGraphics, mouseX.toInt(), mouseY.toInt(), 0f)
    }

    override fun getRecipeType(): RecipeType<FissionFuelInfo> = RECIPE_TYPE
    override fun getTitle(): Component = Component.translatable("fission.fuel.name")
    override fun getWidth(): Int = FissionFuelInfoWidget.WIDTH
    override fun getHeight(): Int = FissionFuelInfoWidget.HEIGHT
    override fun getIcon(): IDrawable = icon

    companion object {
        val RECIPE_TYPE: RecipeType<FissionFuelInfo> =
            RecipeType<FissionFuelInfo>(scId("fission_fuel"), FissionFuelInfo::class.java)

        fun registerRecipes(registry: IRecipeRegistration) {
            val infos = buildList<FissionFuelInfo> {
                for (fuel in FissionFuelRegistry.allFissionableRods) {
                    add(FissionFuelInfo(fuel))
                }
            }
            registry.addRecipes(RECIPE_TYPE, infos)
        }

        fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
            registration.addRecipeCatalyst(ScritMachines.FISSION_REACTOR.asStack(), RECIPE_TYPE)
        }
    }
}
