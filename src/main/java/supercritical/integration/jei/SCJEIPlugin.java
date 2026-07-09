package supercritical.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supercritical.BuildConfig;
import supercritical.integration.jei.basic.CoolantCategory;
import supercritical.integration.jei.basic.FissionFuelCategory;
import supercritical.integration.jei.basic.ModeratorCategory;

@JeiPlugin
public class SCJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(BuildConfig.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        IJeiHelpers helpers = registration.getJeiHelpers();
        registration.addRecipeCategories(new FissionFuelCategory(helpers));
        registration.addRecipeCategories(new CoolantCategory(helpers));
        registration.addRecipeCategories(new ModeratorCategory(helpers));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        FissionFuelCategory.registerRecipes(registration);
        CoolantCategory.registerRecipes(registration);
        ModeratorCategory.registerRecipes(registration);
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        FissionFuelCategory.registerRecipeCatalysts(registration);
        CoolantCategory.registerRecipeCatalysts(registration);
        ModeratorCategory.registerRecipeCatalysts(registration);
    }
}
