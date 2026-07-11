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
import supercritical.api.nuclear.fission.ModeratorRegistry
import supercritical.common.registry.SCMachines

class ModeratorCategory(helpers: IJeiHelpers) : IRecipeCategory<ModeratorInfo?> {
    private val icon: IDrawable
    private val slot: IDrawable

    init {
        val guiHelper = helpers.guiHelper
        this.icon = guiHelper.createDrawableItemStack(SCMachines.FISSION_REACTOR.asStack())
        this.slot = IGui2IDrawable.toDrawable(GuiTextures.SLOT, 18, 18)
    }

    override fun setRecipe(
        builder: IRecipeLayoutBuilder, recipe: ModeratorInfo?,
        focuses: IFocusGroup
    ) {
        val info = recipe ?: return
        builder.addSlot(RecipeIngredientRole.INPUT, 78, 9).addItemStack(info.stack)
    }

    override fun draw(
        recipe: ModeratorInfo?, recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics, mouseX: Double, mouseY: Double
    ) {
        slot.draw(guiGraphics, 77, 8)
        recipe?.drawInfo(guiGraphics, Minecraft.getInstance())
    }

    override fun getRecipeType(): RecipeType<ModeratorInfo?> {
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
        val RECIPE_TYPE: RecipeType<ModeratorInfo?> =
            RecipeType<ModeratorInfo?>(ResourceLocation(BuildConfig.MOD_ID, "moderator"), ModeratorInfo::class.java)

        fun registerRecipes(registry: IRecipeRegistration) {
            val infos = buildList<ModeratorInfo?> {
                for (block in ModeratorRegistry.allModerators) {
                    if (block != null) add(ModeratorInfo(block))
                }
            }
            registry.addRecipes<ModeratorInfo?>(RECIPE_TYPE, infos)
        }

        fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
            registration.addRecipeCatalyst(SCMachines.FISSION_REACTOR.asStack(), RECIPE_TYPE)
            registration.addRecipeCatalyst(SCMachines.MODERATOR_PORT.asStack(), RECIPE_TYPE)
        }
    }
}
