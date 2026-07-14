package io.github.symmetricdevs.supercritical.integration.jei.basic

import io.github.symmetricdevs.supercritical.api.nuclear.fission.ModeratorRegistry
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.xei.ModeratorInfo
import io.github.symmetricdevs.supercritical.integration.xei.widgets.ModeratorInfoWidget
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

class ModeratorCategory(helpers: IJeiHelpers) : IRecipeCategory<ModeratorInfo> {
    private val icon: IDrawable =
        helpers.guiHelper.createDrawableItemStack(ScritMachines.FISSION_REACTOR.asStack())

    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: ModeratorInfo, focuses: IFocusGroup) {
        builder.addSlot(
            RecipeIngredientRole.INPUT,
            ModeratorInfoWidget.INGREDIENT_INPUT_X, ModeratorInfoWidget.INGREDIENT_INPUT_Y
        ).addItemStack(recipe.stack)
    }

    override fun draw(
        recipe: ModeratorInfo, recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double
    ) {
        // buildSlots = false: JEI registers its own native slot on top, so the shared widget
        // only contributes the slot background + stat text (no double-drawn content).
        ModeratorInfoWidget(recipe, buildSlots = false)
            .drawInBackground(guiGraphics, mouseX.toInt(), mouseY.toInt(), 0f)
    }

    override fun getRecipeType(): RecipeType<ModeratorInfo> = RECIPE_TYPE
    override fun getTitle(): Component = Component.translatable("fission.moderator.name")
    override fun getWidth(): Int = ModeratorInfoWidget.WIDTH
    override fun getHeight(): Int = ModeratorInfoWidget.HEIGHT
    override fun getIcon(): IDrawable = icon

    companion object {
        val RECIPE_TYPE: RecipeType<ModeratorInfo> =
            RecipeType<ModeratorInfo>(scId("moderator"), ModeratorInfo::class.java)

        fun registerRecipes(registry: IRecipeRegistration) {
            val infos = buildList<ModeratorInfo> {
                for (block in ModeratorRegistry.allModerators) {
                    if (block != null) add(ModeratorInfo(block))
                }
            }
            registry.addRecipes(RECIPE_TYPE, infos)
        }

        fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
            registration.addRecipeCatalyst(ScritMachines.FISSION_REACTOR.asStack(), RECIPE_TYPE)
            registration.addRecipeCatalyst(ScritMachines.MODERATOR_PORT.asStack(), RECIPE_TYPE)
        }
    }
}
