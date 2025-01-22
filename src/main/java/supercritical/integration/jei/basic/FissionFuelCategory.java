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

public class FissionFuelCategory extends BasicRecipeCategory<FissionFuelInfo, FissionFuelInfo> {

    private final IDrawable icon;
    protected final IDrawable slot;
    private final IDrawable arrow;

    public FissionFuelCategory(IGuiHelper guiHelper) {
        super("fission_fuel", "fission.fuel.name", guiHelper.createBlankDrawable(176, 90), guiHelper);

        this.icon = guiHelper.createDrawableIngredient(SCMetaTileEntities.FISSION_REACTOR.getStackForm());
        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18)
                .build();
        this.arrow = guiHelper.drawableBuilder(GuiTextures.PROGRESS_BAR_ARROW.imageLocation, 0, 20, 20, 20)
                .setTextureSize(20, 40).build();
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FissionFuelInfo recipeWrapper,
                          @NotNull IIngredients ingredients) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();

        itemStackGroup.init(0, true, 54, 8);
        itemStackGroup.set(0, recipeWrapper.rod);
        itemStackGroup.init(1, true, 104, 8);
        itemStackGroup.set(1, recipeWrapper.depletedRod);
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {
        slot.draw(minecraft, 54, 8);
        slot.draw(minecraft, 104, 8);
        arrow.draw(minecraft, 77, 6);
    }

    @NotNull
    @Override
    public IRecipeWrapper getRecipeWrapper(@NotNull FissionFuelInfo recipe) {
        return recipe;
    }

    @NotNull
    @Override
    public String getModName() {
        return SCValues.MODID;
    }
}
