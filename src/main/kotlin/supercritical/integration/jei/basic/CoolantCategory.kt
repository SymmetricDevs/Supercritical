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
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supercritical.BuildConfig;
import supercritical.api.nuclear.fission.CoolantRegistry;
import supercritical.api.nuclear.fission.ICoolantStats;
import supercritical.common.registry.SCMachines;

import java.util.ArrayList;
import java.util.List;

public final class CoolantCategory implements IRecipeCategory<CoolantInfo> {

    public static final RecipeType<CoolantInfo> RECIPE_TYPE =
            new RecipeType<>(new ResourceLocation(BuildConfig.MOD_ID, "coolant"), CoolantInfo.class);

    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable arrow;

    public CoolantCategory(IJeiHelpers helpers) {
        var guiHelper = helpers.getGuiHelper();
        this.icon = guiHelper.createDrawableItemStack(SCMachines.FISSION_REACTOR.asStack());
        this.slot = IGui2IDrawable.toDrawable(GuiTextures.SLOT, 18, 18);
        this.arrow = IGui2IDrawable.toDrawable(GuiTextures.PROGRESS_BAR_ARROW, 20, 20);
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        List<CoolantInfo> infos = new ArrayList<>();
        for (Fluid coolant : CoolantRegistry.getAllCoolants()) {
            ICoolantStats stats = CoolantRegistry.getCoolant(coolant);
            if (stats == null) continue;
            infos.add(new CoolantInfo(coolant, stats.getHotCoolant()));
        }
        registry.addRecipes(RECIPE_TYPE, infos);
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(SCMachines.FISSION_REACTOR.asStack(), RECIPE_TYPE);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CoolantInfo recipe,
                          @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 55, 9).addFluidStack(recipe.coolant.getFluid(), recipe.coolant.getAmount());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 9).addFluidStack(recipe.hotCoolant.getFluid(), recipe.hotCoolant.getAmount());
    }

    @Override
    public void draw(@NotNull CoolantInfo recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        slot.draw(guiGraphics, 54, 8);
        slot.draw(guiGraphics, 104, 8);
        arrow.draw(guiGraphics, 77, 6);
        recipe.drawInfo(guiGraphics, Minecraft.getInstance());
    }

    @NotNull
    @Override
    public RecipeType<CoolantInfo> getRecipeType() {
        return RECIPE_TYPE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("fission.coolant.name");
    }

    @Override
    public int getWidth() {
        return 176;
    }

    @Override
    public int getHeight() {
        return 90;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
