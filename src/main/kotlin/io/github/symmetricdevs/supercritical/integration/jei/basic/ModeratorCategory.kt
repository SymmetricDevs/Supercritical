package io.github.symmetricdevs.supercritical.integration.jei.basic

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.jei.IGui2IDrawable
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ModeratorRegistry
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
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
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

private fun Component.withDefaultColor(): Component =
    if (style.color == null) copy().withStyle(ChatFormatting.BLACK) else this

class ModeratorCategory(helpers: IJeiHelpers) : IRecipeCategory<ModeratorInfo> {
    private val icon: IDrawable
    private val slot: IDrawable

    init {
        val guiHelper = helpers.guiHelper
        this.icon = guiHelper.createDrawableItemStack(ScritMachines.FISSION_REACTOR.asStack())
        this.slot = IGui2IDrawable.toDrawable(GuiTextures.SLOT, 18, 18)
    }

    override fun setRecipe(
        builder: IRecipeLayoutBuilder, recipe: ModeratorInfo,
        focuses: IFocusGroup
    ) {
        builder.addSlot(RecipeIngredientRole.INPUT, 78, 9).addItemStack(recipe.stack)
    }

    override fun draw(
        recipe: ModeratorInfo, recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double
    ) {
        slot.draw(guiGraphics, 77, 8)

        val group = WidgetGroup(0, 0, width, height)
        recipe.textLines.forEachIndexed { index, line ->
            group.addWidget(LabelWidget(0, 40 + index * 10, line.withDefaultColor()).setDropShadow(false))
        }
        group.drawInBackground(guiGraphics, mouseX.toInt(), mouseY.toInt(), 0f)
    }

    override fun getRecipeType(): RecipeType<ModeratorInfo> {
        return RECIPE_TYPE
    }

    override fun getTitle(): Component {
        return Component.translatable("fission.moderator.name")
    }

    override fun getWidth(): Int {
        return 176
    }

    override fun getHeight(): Int {
        return 70
    }

    override fun getIcon(): IDrawable {
        return icon
    }

    companion object {
        val RECIPE_TYPE: RecipeType<ModeratorInfo> =
            RecipeType<ModeratorInfo>(scId("moderator"), ModeratorInfo::class.java)

        fun registerRecipes(registry: IRecipeRegistration) {
            val infos = buildList<ModeratorInfo> {
                for (block in ModeratorRegistry.allModerators) {
                    if (block != null) add(ModeratorInfo(block))
                }
            }
            registry.addRecipes<ModeratorInfo>(RECIPE_TYPE, infos)
        }

        fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
            registration.addRecipeCatalyst(ScritMachines.FISSION_REACTOR.asStack(), RECIPE_TYPE)
            registration.addRecipeCatalyst(ScritMachines.MODERATOR_PORT.asStack(), RECIPE_TYPE)
        }
    }
}
