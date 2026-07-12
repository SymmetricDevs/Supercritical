package io.github.symmetricdevs.supercritical.integration.jei.basic

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.jei.IGui2IDrawable
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
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import io.github.symmetricdevs.supercritical.BuildConfig
import io.github.symmetricdevs.supercritical.api.nuclear.fission.CoolantRegistry
import io.github.symmetricdevs.supercritical.common.data.ScritMachines

class CoolantCategory(helpers: IJeiHelpers) : IRecipeCategory<CoolantInfo> {
    private val icon: IDrawable
    private val slot: IDrawable
    private val arrow: IDrawable

    init {
        val guiHelper = helpers.guiHelper
        this.icon = guiHelper.createDrawableItemStack(ScritMachines.FISSION_REACTOR.asStack())
        this.slot = IGui2IDrawable.toDrawable(GuiTextures.SLOT, 18, 18)
        this.arrow = IGui2IDrawable.toDrawable(GuiTextures.PROGRESS_BAR_ARROW, 20, 20)
    }

    override fun setRecipe(
        builder: IRecipeLayoutBuilder, recipe: CoolantInfo,
        focuses: IFocusGroup
    ) {
        builder.addSlot(RecipeIngredientRole.INPUT, 55, 9)
            .addFluidStack(recipe.coolant.fluid, recipe.coolant.amount.toLong())
        builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 9)
            .addFluidStack(recipe.hotCoolant.fluid, recipe.hotCoolant.amount.toLong())
    }

    override fun draw(
        recipe: CoolantInfo, recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double
    ) {
        slot.draw(guiGraphics, 54, 8)
        slot.draw(guiGraphics, 104, 8)
        arrow.draw(guiGraphics, 77, 6)
        recipe.drawInfo(guiGraphics, Minecraft.getInstance())
    }

    override fun getRecipeType(): RecipeType<CoolantInfo> {
        return RECIPE_TYPE
    }

    override fun getTitle(): Component {
        return Component.translatable("fission.coolant.name")
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
        val RECIPE_TYPE: RecipeType<CoolantInfo> =
            RecipeType<CoolantInfo>(ResourceLocation(BuildConfig.MOD_ID, "coolant"), CoolantInfo::class.java)

        fun registerRecipes(registry: IRecipeRegistration) {
            val infos = buildList {
                for (coolant in CoolantRegistry.allCoolants) {
                    val stats = CoolantRegistry.getCoolant(coolant) ?: continue
                    val hotCoolant = stats.hotCoolant
                    if (coolant != null && hotCoolant != null) add(CoolantInfo(coolant, hotCoolant))
                }
            }
            registry.addRecipes(RECIPE_TYPE, infos)
        }

        fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
            registration.addRecipeCatalyst(ScritMachines.FISSION_REACTOR.asStack(), RECIPE_TYPE)
        }
    }
}
