package io.github.symmetricdevs.supercritical.integration.jei.basic

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.jei.IGui2IDrawable
import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionFuelRegistry
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

class FissionFuelCategory(helpers: IJeiHelpers) : IRecipeCategory<FissionFuelInfo> {
    private val icon: IDrawable
    private val slot: IDrawable
    private val progressArrow: ProgressTexture

    init {
        val guiHelper = helpers.guiHelper
        this.icon = guiHelper.createDrawableItemStack(ScritMachines.FISSION_REACTOR.asStack())
        this.slot = IGui2IDrawable.toDrawable(GuiTextures.SLOT, 18, 18)
        this.progressArrow = ProgressTexture(
            GuiTextures.PROGRESS_BAR_ARROW.getSubTexture(0.0, 0.0, 1.0, 0.5),
            GuiTextures.PROGRESS_BAR_ARROW.getSubTexture(0.0, 0.5, 1.0, 0.5)
        )
        this.progressArrow.setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT)
    }

    override fun setRecipe(
        builder: IRecipeLayoutBuilder, recipe: FissionFuelInfo,
        focuses: IFocusGroup
    ) {
        builder.addSlot(RecipeIngredientRole.INPUT, 55, 9).addItemStack(recipe.rod)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 9).addItemStacks(recipe.depletedRods)
    }

    override fun draw(
        recipe: FissionFuelInfo, recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double
    ) {
        slot.draw(guiGraphics, 54, 8)
        slot.draw(guiGraphics, 104, 8)
        progressArrow.setProgress(ProgressWidget.JEIProgress.asDouble)
        progressArrow.draw(guiGraphics, mouseX.toInt(), mouseY.toInt(), 77.0f, 6.0f, 20, 20)

        val group = WidgetGroup(0, 0, width, height)
        recipe.textLines.forEachIndexed { index, line ->
            group.addWidget(LabelWidget(0, 40 + index * 10, line.withDefaultColor()).setDropShadow(false))
        }
        group.drawInBackground(guiGraphics, mouseX.toInt(), mouseY.toInt(), 0f)
    }

    override fun getRecipeType(): RecipeType<FissionFuelInfo> {
        return RECIPE_TYPE
    }

    override fun getTitle(): Component {
        return Component.translatable("fission.fuel.name")
    }

    override fun getWidth(): Int {
        return 176
    }

    override fun getHeight(): Int {
        return 90
    }

    override fun getIcon(): IDrawable {
        return icon
    }

    companion object {
        val RECIPE_TYPE: RecipeType<FissionFuelInfo> = RecipeType<FissionFuelInfo>(
            scId("fission_fuel"),
            FissionFuelInfo::class.java
        )

        fun registerRecipes(registry: IRecipeRegistration) {
            val infos = buildList<FissionFuelInfo> {
                for (fuel in FissionFuelRegistry.allFissionableRods) {
                    add(FissionFuelInfo(fuel))
                }
            }
            registry.addRecipes<FissionFuelInfo>(RECIPE_TYPE, infos)
        }

        fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
            registration.addRecipeCatalyst(ScritMachines.FISSION_REACTOR.asStack(), RECIPE_TYPE)
        }
    }
}
