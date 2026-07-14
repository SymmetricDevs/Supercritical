package io.github.symmetricdevs.supercritical.integration.rei.basic

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.rei.IGui2Renderer
import com.lowdragmc.lowdraglib.rei.ModularDisplay
import com.lowdragmc.lowdraglib.rei.ModularUIDisplayCategory
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ModeratorRegistry
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.xei.ModeratorInfo
import io.github.symmetricdevs.supercritical.integration.xei.widgets.ModeratorInfoWidget
import io.github.symmetricdevs.supercritical.util.scId
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import net.minecraft.network.chat.Component

object ModeratorDisplayCategory : ModularUIDisplayCategory<ModeratorDisplay>() {
    val ID: CategoryIdentifier<ModeratorDisplay> = CategoryIdentifier.of(scId("moderator"))

    override fun getCategoryIdentifier(): CategoryIdentifier<out ModeratorDisplay> = ID
    override fun getTitle(): Component = Component.translatable("fission.moderator.name")
    override fun getIcon(): Renderer =
        IGui2Renderer.toDrawable(ItemStackTexture(ScritMachines.FISSION_REACTOR.asStack()))

    override fun getDisplayWidth(display: ModeratorDisplay): Int = ModeratorInfoWidget.WIDTH + 8
    override fun getDisplayHeight(): Int = ModeratorInfoWidget.HEIGHT + 8

    fun registerDisplays(registry: DisplayRegistry) {
        for (block in ModeratorRegistry.allModerators) {
            if (block != null) registry.add(ModeratorDisplay(ModeratorInfo(block)))
        }
    }
}

/**
 * REI display for a moderator block entry. See [CoolantDisplay] for the auto-extraction contract.
 */
class ModeratorDisplay(info: ModeratorInfo) :
    ModularDisplay<WidgetGroup>({ ModeratorInfoWidget(info) }, ModeratorDisplayCategory.ID)