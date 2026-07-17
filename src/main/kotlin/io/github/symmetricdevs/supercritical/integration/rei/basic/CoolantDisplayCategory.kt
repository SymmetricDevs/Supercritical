package io.github.symmetricdevs.supercritical.integration.rei.basic

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.rei.IGui2Renderer
import com.lowdragmc.lowdraglib.rei.ModularDisplay
import com.lowdragmc.lowdraglib.rei.ModularUIDisplayCategory
import io.github.symmetricdevs.supercritical.common.data.ScritPropertyKeys
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.xei.CoolantInfo
import io.github.symmetricdevs.supercritical.integration.xei.widgets.CoolantInfoWidget
import io.github.symmetricdevs.supercritical.util.scId
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import net.minecraft.network.chat.Component

/**
 * REI category for coolant heating. Extends LDLib's [ModularUIDisplayCategory], so `setupDisplay`
 * is inherited and delegates to [com.lowdragmc.lowdraglib.rei.ModularDisplay.createWidget].
 */
object CoolantDisplayCategory : ModularUIDisplayCategory<CoolantDisplay>() {
    val ID: CategoryIdentifier<CoolantDisplay> = CategoryIdentifier.of(scId("coolant"))

    override fun getCategoryIdentifier(): CategoryIdentifier<out CoolantDisplay> = ID
    override fun getTitle(): Component = Component.translatable("fission.coolant.name")
    override fun getIcon(): Renderer =
        IGui2Renderer.toDrawable(ItemStackTexture(ScritMachines.FISSION_REACTOR.asStack()))

    override fun getDisplayWidth(display: CoolantDisplay): Int = CoolantInfoWidget.WIDTH + 8
    override fun getDisplayHeight(): Int = CoolantInfoWidget.HEIGHT + 8

    fun registerDisplays(registry: DisplayRegistry) {
        for (material in GTCEuAPI.materialManager.registeredMaterials) {
            if (!material.hasProperty(ScritPropertyKeys.COOLANT)) continue
            val property = material.getProperty(ScritPropertyKeys.COOLANT)
            val coolant = property.coolantFluid
            val hotCoolant = property.hotCoolantFluid
            registry.add(CoolantDisplay(CoolantInfo(coolant, hotCoolant)))
        }
    }
}

/**
 * REI display for a coolant heating entry. Extends LDLib's [com.lowdragmc.lowdraglib.rei.ModularDisplay], which auto-extracts
 * the widget tree's [com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot] tanks into
 * REI ingredient slots, auto-clears them, and auto-renders the widget via [com.lowdragmc.lowdraglib.jei.ModularWrapper].
 */
class CoolantDisplay(info: CoolantInfo) :
    ModularDisplay<WidgetGroup>({ CoolantInfoWidget(info) }, CoolantDisplayCategory.ID)