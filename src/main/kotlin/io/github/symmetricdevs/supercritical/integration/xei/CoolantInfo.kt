package io.github.symmetricdevs.supercritical.integration.xei

import io.github.symmetricdevs.supercritical.api.fission.stats.CoolantStats
import net.minecraft.network.chat.Component
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack

/**
 * Viewer-neutral data wrapper for a coolant heating entry, shared by JEI/REI/EMI.
 */
class CoolantInfo(coolant: Fluid, hotCoolant: Fluid) {
    val coolant: FluidStack = FluidStack(coolant, 1000)
    val hotCoolant: FluidStack = FluidStack(hotCoolant, 1000)

    val textLines: List<Component>

    init {
        val stats = CoolantStats.of(this.coolant.fluid)
        textLines = if (stats != null) buildList(5) {
            add(
                Component.translatable(
                    "supercritical.coolant.exit_temp",
                    stats.hotCoolantFluid?.fluidType?.temperature ?: 0
                )
            )
            add(
                Component.translatable(
                    "supercritical.coolant.heat_capacity",
                    stats.specificHeatCapacity.toInt()
                )
            )
            add(
                Component.translatable(
                    "supercritical.coolant.cooling_factor",
                    stats.coolingFactor.toInt()
                )
            )
            add(
                Component.translatable(
                    "supercritical.coolant.moderation_factor",
                    stats.moderatorFactor.toInt()
                )
            )
            if (stats.accumulatesHydrogen) {
                add(Component.translatable("supercritical.coolant.accumulates_hydrogen"))
            }
        } else emptyList()
    }
}
