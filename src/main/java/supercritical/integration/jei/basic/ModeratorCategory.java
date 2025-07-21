package supercritical.integration.jei.basic;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.gui.GuiTextures;
import gregtech.integration.jei.basic.BasicRecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import supercritical.SCValues;
import supercritical.common.metatileentities.SCMetaTileEntities;

public class ModeratorCategory extends BasicRecipeCategory<ModeratorInfo, ModeratorInfo> {

    private final IDrawable icon;
    protected final IDrawable slot;

    public ModeratorCategory(IGuiHelper guiHelper) {
        super("moderator", "fission.moderator.name", guiHelper.createBlankDrawable(176, 70), guiHelper);

        this.icon = guiHelper.createDrawableIngredient(SCMetaTileEntities.FISSION_REACTOR.getStackForm());
        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18)
                .build();
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ModeratorInfo recipeWrapper, @NotNull IIngredients ingredients) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();

        itemStackGroup.init(0, true, 77, 8);
        itemStackGroup.set(0, recipeWrapper.stack);
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {
        slot.draw(minecraft, 77, 8);
    }

    @NotNull
    @Override
    public IRecipeWrapper getRecipeWrapper(@NotNull ModeratorInfo recipe) {
        return recipe;
    }

    @NotNull
    @Override
    public String getModName() {
        return SCValues.MODID;
    }
}
