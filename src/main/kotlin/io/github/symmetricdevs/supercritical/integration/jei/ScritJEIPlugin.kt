package io.github.symmetricdevs.supercritical.integration.jei

import io.github.symmetricdevs.supercritical.integration.jei.basic.CoolantCategory
import io.github.symmetricdevs.supercritical.integration.jei.basic.FissionFuelCategory
import io.github.symmetricdevs.supercritical.integration.jei.basic.ModeratorCategory
import io.github.symmetricdevs.supercritical.util.scId
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.resources.ResourceLocation

@JeiPlugin
class ScritJEIPlugin : IModPlugin {
    override fun getPluginUid(): ResourceLocation = scId("jei_plugin")

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        val helpers = registration.jeiHelpers
        registration.addRecipeCategories(FissionFuelCategory(helpers))
        registration.addRecipeCategories(CoolantCategory(helpers))
        registration.addRecipeCategories(ModeratorCategory(helpers))
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        FissionFuelCategory.registerRecipes(registration)
        CoolantCategory.registerRecipes(registration)
        ModeratorCategory.registerRecipes(registration)
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        FissionFuelCategory.registerRecipeCatalysts(registration)
        CoolantCategory.registerRecipeCatalysts(registration)
        ModeratorCategory.registerRecipeCatalysts(registration)
    }
}
