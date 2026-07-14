package io.github.symmetricdevs.supercritical.integration.rei

import io.github.symmetricdevs.supercritical.integration.rei.basic.CoolantDisplayCategory
import io.github.symmetricdevs.supercritical.integration.rei.basic.FissionFuelDisplayCategory
import io.github.symmetricdevs.supercritical.integration.rei.basic.ModeratorDisplayCategory
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.forge.REIPluginClient

/**
 * Roughly Enough Items integration. Discovered via the [@REIPluginClient] annotation, so this
 * class must not be referenced from the main mod class — REI loads it only when present.
 *
 * Categories/displays are thin wrappers over LDLib's [com.lowdragmc.lowdraglib.rei.ModularDisplay]
 * / [com.lowdragmc.lowdraglib.rei.ModularUIDisplayCategory], which auto-extract the shared
 * [io.github.symmetricdevs.supercritical.integration.xei.widgets] ingredient slots.
 */
@REIPluginClient
class ScritREIPlugin : REIClientPlugin {

    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(CoolantDisplayCategory)
        registry.add(FissionFuelDisplayCategory)
        registry.add(ModeratorDisplayCategory)
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        CoolantDisplayCategory.registerDisplays(registry)
        FissionFuelDisplayCategory.registerDisplays(registry)
        ModeratorDisplayCategory.registerDisplays(registry)
    }
}
