package supercritical.integration.jei.basic

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import supercritical.api.nuclear.fission.CoolantRegistry

class CoolantInfo(coolant: Fluid, hotCoolant: Fluid) {
    val coolant: FluidStack
    val hotCoolant: FluidStack

    private val temps: Component
    private val heatCapacity: Component
    private val heatTransfer: Component
    private val moderation: Component
    private var hydrogen: Component? = null

    init {
        this.coolant = FluidStack(coolant, 1000)
        this.hotCoolant = FluidStack(hotCoolant, 1000)

        val stats = CoolantRegistry.getCoolant(this.coolant.getFluid())
        if (stats != null) {
            this.temps = Component.translatable(
                "supercritical.coolant.exit_temp",
                stats.getHotCoolant().getFluidType().getTemperature()
            )
            this.heatCapacity = Component.translatable(
                "supercritical.coolant.heat_capacity",
                stats.getSpecificHeatCapacity().toInt()
            )
            this.heatTransfer = Component.translatable(
                "supercritical.coolant.cooling_factor",
                stats.getCoolingFactor().toInt()
            )
            this.moderation = Component.translatable(
                "supercritical.coolant.moderation_factor",
                stats.getModeratorFactor().toInt()
            )
            this.hydrogen = if (stats.accumulatesHydrogen())
                Component.translatable("supercritical.coolant.accumulates_hydrogen")
            else
                null
        } else {
            this.temps = Component.empty()
            this.heatCapacity = Component.empty()
            this.heatTransfer = Component.empty()
            this.moderation = Component.empty()
            this.hydrogen = null
        }
    }

    fun drawInfo(graphics: GuiGraphics, minecraft: Minecraft) {
        val font = minecraft.font
        val start = 40
        val lineHeight = font.lineHeight + 1
        val color = -0xeeeeef

        graphics.drawString(font, temps, 0, start, color, false)
        graphics.drawString(font, heatCapacity, 0, start + lineHeight, color, false)
        graphics.drawString(font, heatTransfer, 0, start + 2 * lineHeight, color, false)
        graphics.drawString(font, moderation, 0, start + 3 * lineHeight, color, false)

        if (hydrogen != null) {
            graphics.drawString(font, hydrogen, 0, start + 4 * lineHeight, color, false)
        }
    }
}
