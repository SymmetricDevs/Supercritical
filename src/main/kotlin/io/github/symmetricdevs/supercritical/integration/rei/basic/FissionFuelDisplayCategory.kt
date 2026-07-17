package io.github.symmetricdevs.supercritical.integration.rei.basic

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.rei.IGui2Renderer
import com.lowdragmc.lowdraglib.rei.ModularDisplay
import com.lowdragmc.lowdraglib.rei.ModularUIDisplayCategory
import io.github.symmetricdevs.supercritical.common.data.ScritItems
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.xei.FissionFuelInfo
import io.github.symmetricdevs.supercritical.integration.xei.widgets.FissionFuelInfoWidget
import io.github.symmetricdevs.supercritical.util.scId
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import net.minecraft.network.chat.Component

object FissionFuelDisplayCategory : ModularUIDisplayCategory<FissionFuelDisplay>() {
    val ID: CategoryIdentifier<FissionFuelDisplay> = CategoryIdentifier.of(scId("fission_fuel"))

    override fun getCategoryIdentifier(): CategoryIdentifier<out FissionFuelDisplay> = ID
    override fun getTitle(): Component = Component.translatable("fission.fuel.name")
    override fun getIcon(): Renderer =
        IGui2Renderer.toDrawable(ItemStackTexture(ScritMachines.FISSION_REACTOR.asStack()))

    override fun getDisplayWidth(display: FissionFuelDisplay): Int = FissionFuelInfoWidget.WIDTH + 8
    override fun getDisplayHeight(): Int = FissionFuelInfoWidget.HEIGHT + 8

    fun registerDisplays(registry: DisplayRegistry) {
        for (fuelItems in ScritItems.NUCLEAR_FUEL_ITEMS.values) {
            registry.add(FissionFuelDisplay(FissionFuelInfo(fuelItems.fuelRod.get().defaultInstance)))
        }
    }
}

/**
 * REI display for a fission fuel rod entry. See [CoolantDisplay] for the auto-extraction contract.
 */
class FissionFuelDisplay(info: FissionFuelInfo) :
    ModularDisplay<WidgetGroup>({ FissionFuelInfoWidget(info) }, FissionFuelDisplayCategory.ID)