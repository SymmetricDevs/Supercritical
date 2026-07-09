package supercritical.integration.jei.basic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import supercritical.api.nuclear.fission.IModeratorStats;
import supercritical.api.nuclear.fission.ModeratorRegistry;

public final class ModeratorInfo {

    public final BlockState blockState;
    public final ItemStack stack;

    private final Component maxTemp;
    private final Component moderationFactor;
    private final Component absorptionFactor;

    public ModeratorInfo(Block block) {
        this.blockState = block.defaultBlockState();
        this.stack = new ItemStack(block);

        IModeratorStats prop = ModeratorRegistry.getModerator(block);
        if (prop != null) {
            this.maxTemp = Component.translatable("metaitem.nuclear.tooltip.temperature", prop.getMaxTemperature());
            this.moderationFactor = Component.translatable("metaitem.nuclear.tooltip.moderation_factor",
                    (int) prop.getModerationFactor());
            this.absorptionFactor = Component.translatable("metaitem.nuclear.tooltip.absorption_factor",
                    (int) prop.getAbsorptionFactor());
        } else {
            this.maxTemp = Component.empty();
            this.moderationFactor = Component.empty();
            this.absorptionFactor = Component.empty();
        }
    }

    public void drawInfo(GuiGraphics graphics, Minecraft minecraft) {
        Font font = minecraft.font;
        int start = 40;
        int lineHeight = font.lineHeight + 1;
        int color = 0xFF111111;

        graphics.drawString(font, maxTemp, 0, start, color, false);
        graphics.drawString(font, moderationFactor, 0, start + lineHeight, color, false);
        graphics.drawString(font, absorptionFactor, 0, start + 2 * lineHeight, color, false);
    }
}
