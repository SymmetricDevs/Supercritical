package io.github.symmetricdevs.supercritical.integration.xei

import io.github.symmetricdevs.supercritical.api.fission.stats.FissionFuelStats
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

/**
 * Viewer-neutral data wrapper for a fission fuel rod entry, shared by JEI/REI/EMI.
 */
class FissionFuelInfo(rod: ItemStack) {
    val rod: ItemStack
    val depletedRods: MutableList<ItemStack>
    val textLines: List<Component>

    init {
        this.rod = rod.copy()

        val prop = FissionFuelStats.of(rod)
        if (prop != null) {
            this.depletedRods = prop.depletedFuels
            this.textLines = listOf(
                Component.translatable(
                    "metaitem.nuclear.tooltip.duration",
                    (prop.duration * prop.releasedHeatEnergy).toInt()
                ),
                Component.translatable("metaitem.nuclear.tooltip.temperature", prop.maxTemperature),
                Component.translatable(
                    "metaitem.nuclear.tooltip.cross_section_fast",
                    prop.fastNeutronFissionCrossSection.toInt()
                ),
                Component.translatable(
                    "metaitem.nuclear.tooltip.cross_section_slow",
                    prop.slowNeutronFissionCrossSection.toInt()
                ),
                Component.translatable(
                    "metaitem.nuclear.tooltip.neutron_time." + prop.neutronGenerationTimeCategory,
                    prop.neutronGenerationTime.toInt()
                )
            )
        } else {
            this.depletedRods = mutableListOf()
            this.textLines = emptyList()
        }
    }
}
