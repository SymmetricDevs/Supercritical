package supercritical.integration.jei.basic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import supercritical.api.nuclear.fission.FissionFuelRegistry;
import supercritical.api.nuclear.fission.IFissionFuelStats;

import java.util.List;

/**
 * JEI recipe wrapper for a fission fuel rod entry.
 */
public final class FissionFuelInfo {

    public final ItemStack rod;
    public final List<ItemStack> depletedRods;

    private final Component duration;
    private final Component maxTemp;
    private final Component crossSectionFast;
    private final Component crossSectionSlow;
    private final Component neutronGenerationTime;

    public FissionFuelInfo(ItemStack rod) {
        this.rod = rod.copy();

        IFissionFuelStats prop = FissionFuelRegistry.getFissionFuel(rod);
        if (prop != null) {
            this.depletedRods = prop.getDepletedFuels();
            this.duration = Component.translatable("metaitem.nuclear.tooltip.duration",
                    (int) (prop.getDuration() * prop.getReleasedHeatEnergy()));
            this.maxTemp = Component.translatable("metaitem.nuclear.tooltip.temperature", prop.getMaxTemperature());
            this.crossSectionFast = Component.translatable("metaitem.nuclear.tooltip.cross_section_fast",
                    (int) prop.getFastNeutronFissionCrossSection());
            this.crossSectionSlow = Component.translatable("metaitem.nuclear.tooltip.cross_section_slow",
                    (int) prop.getSlowNeutronFissionCrossSection());
            this.neutronGenerationTime = Component.translatable(
                    "metaitem.nuclear.tooltip.neutron_time." + prop.getNeutronGenerationTimeCategory(),
                    (int) prop.getNeutronGenerationTime());
        } else {
            this.depletedRods = List.of();
            this.duration = Component.empty();
            this.maxTemp = Component.empty();
            this.crossSectionFast = Component.empty();
            this.crossSectionSlow = Component.empty();
            this.neutronGenerationTime = Component.empty();
        }
    }

    public void drawInfo(GuiGraphics graphics, Minecraft minecraft) {
        Font font = minecraft.font;
        int start = 40;
        int lineHeight = font.lineHeight + 1;
        int color = 0xFF111111;

        graphics.drawString(font, duration, 0, start, color, false);
        graphics.drawString(font, maxTemp, 0, start + lineHeight, color, false);
        graphics.drawString(font, crossSectionFast, 0, start + 2 * lineHeight, color, false);
        graphics.drawString(font, crossSectionSlow, 0, start + 3 * lineHeight, color, false);
        graphics.drawString(font, neutronGenerationTime, 0, start + 4 * lineHeight, color, false);
    }
}
