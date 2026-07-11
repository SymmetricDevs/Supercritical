package supercritical.integration.jei.basic

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
import supercritical.BuildConfig
import supercritical.api.nuclear.fission.FissionFuelRegistry
import supercritical.common.registry.SCMachines

class FissionFuelCategory(helpers: IJeiHelpers) : IRecipeCategory<FissionFuelInfo?> {
    private val icon: IDrawable
    private val slot: IDrawable
    private val arrow: IDrawable

    init {
        val guiHelper = helpers.guiHelper
        this.icon = guiHelper.createDrawableItemStack(SCMachines.FISSION_REACTOR.asStack())
        this.slot = IGui2IDrawable.toDrawable(GuiTextures.SLOT, 18, 18)
        this.arrow = IGui2IDrawable.toDrawable(GuiTextures.PROGRESS_BAR_ARROW, 20, 20)
    }

    override fun setRecipe(
        builder: IRecipeLayoutBuilder, recipe: FissionFuelInfo?,
        focuses: IFocusGroup
    ) {
        val info = recipe ?: return
        builder.addSlot(RecipeIngredientRole.INPUT, 55, 9).addItemStack(info.rod)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 9).addItemStacks(info.depletedRods)
    }

    override fun draw(
        recipe: FissionFuelInfo?, recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double
    ) {
        slot.draw(guiGraphics, 54, 8)
        slot.draw(guiGraphics, 104, 8)
        arrow.draw(guiGraphics, 77, 6)
        recipe?.drawInfo(guiGraphics, Minecraft.getInstance())
    }

    override fun getRecipeType(): RecipeType<FissionFuelInfo?> {
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
        val RECIPE_TYPE: RecipeType<FissionFuelInfo?> = RecipeType<FissionFuelInfo?>(
            ResourceLocation(BuildConfig.MOD_ID, "fission_fuel"),
            FissionFuelInfo::class.java
        )

        fun registerRecipes(registry: IRecipeRegistration) {
            val infos = buildList<FissionFuelInfo?> {
                for (fuel in FissionFuelRegistry.allFissionableRods) {
                    if (fuel != null) add(FissionFuelInfo(fuel))
                }
            }
            registry.addRecipes<FissionFuelInfo?>(RECIPE_TYPE, infos)
        }

        fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
            registration.addRecipeCatalyst(SCMachines.FISSION_REACTOR.asStack(), RECIPE_TYPE)
        }
    }
}
