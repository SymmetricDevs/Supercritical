package supercritical.integration.jei.basic

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import supercritical.api.nuclear.fission.FissionFuelRegistry

/**
 * JEI recipe wrapper for a fission fuel rod entry.
 */
class FissionFuelInfo(rod: ItemStack) {
    val rod: ItemStack
    val depletedRods: MutableList<ItemStack?>?

    private val duration: Component
    private val maxTemp: Component
    private val crossSectionFast: Component
    private val crossSectionSlow: Component
    private val neutronGenerationTime: Component

    init {
        this.rod = rod.copy()

        val prop = FissionFuelRegistry.getFissionFuel(rod)
        if (prop != null) {
            this.depletedRods = prop.depletedFuels
            this.duration = Component.translatable(
                "metaitem.nuclear.tooltip.duration",
                (prop.duration * prop.releasedHeatEnergy).toInt()
            )
            this.maxTemp = Component.translatable("metaitem.nuclear.tooltip.temperature", prop.maxTemperature)
            this.crossSectionFast = Component.translatable(
                "metaitem.nuclear.tooltip.cross_section_fast",
                prop.fastNeutronFissionCrossSection.toInt()
            )
            this.crossSectionSlow = Component.translatable(
                "metaitem.nuclear.tooltip.cross_section_slow",
                prop.slowNeutronFissionCrossSection.toInt()
            )
            this.neutronGenerationTime = Component.translatable(
                "metaitem.nuclear.tooltip.neutron_time." + prop.neutronGenerationTimeCategory,
                prop.neutronGenerationTime.toInt()
            )
        } else {
            this.depletedRods = mutableListOf<ItemStack?>()
            this.duration = Component.empty()
            this.maxTemp = Component.empty()
            this.crossSectionFast = Component.empty()
            this.crossSectionSlow = Component.empty()
            this.neutronGenerationTime = Component.empty()
        }
    }

    fun drawInfo(graphics: GuiGraphics, minecraft: Minecraft) {
        val font = minecraft.font
        val start = 40
        val lineHeight = font.lineHeight + 1
        val color = -0xeeeeef

        graphics.drawString(font, duration, 0, start, color, false)
        graphics.drawString(font, maxTemp, 0, start + lineHeight, color, false)
        graphics.drawString(font, crossSectionFast, 0, start + 2 * lineHeight, color, false)
        graphics.drawString(font, crossSectionSlow, 0, start + 3 * lineHeight, color, false)
        graphics.drawString(font, neutronGenerationTime, 0, start + 4 * lineHeight, color, false)
    }
}
