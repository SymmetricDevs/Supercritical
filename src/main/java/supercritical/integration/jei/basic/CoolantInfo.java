package supercritical.integration.jei.basic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import supercritical.api.nuclear.fission.CoolantRegistry;
import supercritical.api.nuclear.fission.ICoolantStats;

public final class CoolantInfo {

    public final FluidStack coolant;
    public final FluidStack hotCoolant;

    private final Component temps;
    private final Component heatCapacity;
    private final Component heatTransfer;
    private final Component moderation;
    private Component hydrogen;

    public CoolantInfo(Fluid coolant, Fluid hotCoolant) {
        this.coolant = new FluidStack(coolant, 1000);
        this.hotCoolant = new FluidStack(hotCoolant, 1000);

        ICoolantStats stats = CoolantRegistry.getCoolant(this.coolant.getFluid());
        if (stats != null) {
            this.temps = Component.translatable("supercritical.coolant.exit_temp",
                    (int) stats.getHotCoolant().getFluidType().getTemperature());
            this.heatCapacity = Component.translatable("supercritical.coolant.heat_capacity",
                    (int) stats.getSpecificHeatCapacity());
            this.heatTransfer = Component.translatable("supercritical.coolant.cooling_factor",
                    (int) stats.getCoolingFactor());
            this.moderation = Component.translatable("supercritical.coolant.moderation_factor",
                    (int) stats.getModeratorFactor());
            this.hydrogen = stats.accumulatesHydrogen()
                    ? Component.translatable("supercritical.coolant.accumulates_hydrogen")
                    : null;
        } else {
            this.temps = Component.empty();
            this.heatCapacity = Component.empty();
            this.heatTransfer = Component.empty();
            this.moderation = Component.empty();
            this.hydrogen = null;
        }
    }

    public void drawInfo(GuiGraphics graphics, Minecraft minecraft) {
        Font font = minecraft.font;
        int start = 40;
        int lineHeight = font.lineHeight + 1;
        int color = 0xFF111111;

        graphics.drawString(font, temps, 0, start, color, false);
        graphics.drawString(font, heatCapacity, 0, start + lineHeight, color, false);
        graphics.drawString(font, heatTransfer, 0, start + 2 * lineHeight, color, false);
        graphics.drawString(font, moderation, 0, start + 3 * lineHeight, color, false);

        if (hydrogen != null) {
            graphics.drawString(font, hydrogen, 0, start + 4 * lineHeight, color, false);
        }
    }
}
