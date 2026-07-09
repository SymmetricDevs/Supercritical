package supercritical.integration.jei.basic;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.jei.IGui2IDrawable;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supercritical.BuildConfig;
import supercritical.api.nuclear.fission.ModeratorRegistry;
import supercritical.common.registry.SCMachines;

import java.util.ArrayList;
import java.util.List;

public final class ModeratorCategory implements IRecipeCategory<ModeratorInfo> {

    public static final RecipeType<ModeratorInfo> RECIPE_TYPE =
            new RecipeType<>(new ResourceLocation(BuildConfig.MOD_ID, "moderator"), ModeratorInfo.class);

    private final IDrawable icon;
    private final IDrawable slot;

    public ModeratorCategory(IJeiHelpers helpers) {
        var guiHelper = helpers.getGuiHelper();
        this.icon = guiHelper.createDrawableItemStack(SCMachines.FISSION_REACTOR.asStack());
        this.slot = IGui2IDrawable.toDrawable(GuiTextures.SLOT, 18, 18);
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        List<ModeratorInfo> infos = new ArrayList<>();
        for (Block block : ModeratorRegistry.getAllModerators()) {
            infos.add(new ModeratorInfo(block));
        }
        registry.addRecipes(RECIPE_TYPE, infos);
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(SCMachines.FISSION_REACTOR.asStack(), RECIPE_TYPE);
        registration.addRecipeCatalyst(SCMachines.MODERATOR_PORT.asStack(), RECIPE_TYPE);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ModeratorInfo recipe,
                          @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 78, 9).addItemStack(recipe.stack);
    }

    @Override
    public void draw(@NotNull ModeratorInfo recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        slot.draw(guiGraphics, 77, 8);
        recipe.drawInfo(guiGraphics, Minecraft.getInstance());
    }

    @NotNull
    @Override
    public RecipeType<ModeratorInfo> getRecipeType() {
        return RECIPE_TYPE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("fission.moderator.name");
    }

    @Override
    public int getWidth() {
        return 176;
    }

    @Override
    public int getHeight() {
        return 70;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
