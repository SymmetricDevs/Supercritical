package supercritical.integration.jei

import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.resources.ResourceLocation
import supercritical.BuildConfig
import supercritical.integration.jei.basic.CoolantCategory
import supercritical.integration.jei.basic.FissionFuelCategory
import supercritical.integration.jei.basic.ModeratorCategory

@JeiPlugin
class SCJEIPlugin : IModPlugin {
    override fun getPluginUid(): ResourceLocation {
        return ResourceLocation(BuildConfig.MOD_ID, "jei_plugin")
    }

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        val helpers = registration.getJeiHelpers()
        registration.addRecipeCategories(FissionFuelCategory(helpers))
        registration.addRecipeCategories(CoolantCategory(helpers))
        registration.addRecipeCategories(ModeratorCategory(helpers))
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        FissionFuelCategory.Companion.registerRecipes(registration)
        CoolantCategory.Companion.registerRecipes(registration)
        ModeratorCategory.Companion.registerRecipes(registration)
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        FissionFuelCategory.Companion.registerRecipeCatalysts(registration)
        CoolantCategory.Companion.registerRecipeCatalysts(registration)
        ModeratorCategory.Companion.registerRecipeCatalysts(registration)
    }
}
