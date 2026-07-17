package io.github.symmetricdevs.supercritical.integration.jei.basic

import com.gregtechceu.gtceu.api.GTCEuAPI
import io.github.symmetricdevs.supercritical.common.data.ScritPropertyKeys
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.xei.CoolantInfo
import io.github.symmetricdevs.supercritical.integration.xei.widgets.CoolantInfoWidget
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

class CoolantCategory(helpers: IJeiHelpers) : IRecipeCategory<CoolantInfo> {
    private val icon: IDrawable =
        helpers.guiHelper.createDrawableItemStack(ScritMachines.FISSION_REACTOR.asStack())

    override fun setRecipe(builder: IRecipeLayoutBuilder, recipe: CoolantInfo, focuses: IFocusGroup) {
        builder.addSlot(
            RecipeIngredientRole.INPUT,
            CoolantInfoWidget.INGREDIENT_INPUT_X, CoolantInfoWidget.INGREDIENT_INPUT_Y
        ).addFluidStack(recipe.coolant.fluid, recipe.coolant.amount.toLong())
        builder.addSlot(
            RecipeIngredientRole.OUTPUT,
            CoolantInfoWidget.INGREDIENT_OUTPUT_X, CoolantInfoWidget.INGREDIENT_OUTPUT_Y
        ).addFluidStack(recipe.hotCoolant.fluid, recipe.hotCoolant.amount.toLong())
    }

    override fun draw(
        recipe: CoolantInfo, recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double
    ) {
        // buildSlots = false: JEI registers its own native slots on top, so the shared widget
        // only contributes the slot backgrounds + arrow + stat text (no double-drawn content).
        CoolantInfoWidget(recipe, buildSlots = false)
            .drawInBackground(guiGraphics, mouseX.toInt(), mouseY.toInt(), 0f)
    }

    override fun getRecipeType(): RecipeType<CoolantInfo> = RECIPE_TYPE
    override fun getTitle(): Component = Component.translatable("fission.coolant.name")
    override fun getWidth(): Int = CoolantInfoWidget.WIDTH
    override fun getHeight(): Int = CoolantInfoWidget.HEIGHT
    override fun getIcon(): IDrawable = icon

    companion object {
        val RECIPE_TYPE: RecipeType<CoolantInfo> =
            RecipeType<CoolantInfo>(scId("coolant"), CoolantInfo::class.java)

        fun registerRecipes(registry: IRecipeRegistration) {
            val infos = buildList {
                for (material in GTCEuAPI.materialManager.registeredMaterials) {
                    if (!material.hasProperty(ScritPropertyKeys.COOLANT)) continue
                    val property = material.getProperty(ScritPropertyKeys.COOLANT)
                    val coolant = property.coolantFluid
                    val hotCoolant = property.hotCoolantFluid
                    add(CoolantInfo(coolant, hotCoolant))
                }
            }
            registry.addRecipes(RECIPE_TYPE, infos)
        }

        fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
            registration.addRecipeCatalyst(ScritMachines.FISSION_REACTOR.asStack(), RECIPE_TYPE)
        }
    }
}
