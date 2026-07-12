package io.github.symmetricdevs.supercritical.integration.jei.basic

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ModeratorRegistry

class ModeratorInfo(block: Block) {
    val blockState: BlockState
    val stack: ItemStack

    private val maxTemp: Component
    private val moderationFactor: Component
    private val absorptionFactor: Component

    init {
        this.blockState = block.defaultBlockState()
        this.stack = ItemStack(block)

        val prop = ModeratorRegistry.getModerator(block)
        if (prop != null) {
            this.maxTemp = Component.translatable("metaitem.nuclear.tooltip.temperature", prop.maxTemperature)
            this.moderationFactor = Component.translatable(
                "metaitem.nuclear.tooltip.moderation_factor",
                prop.moderationFactor.toInt()
            )
            this.absorptionFactor = Component.translatable(
                "metaitem.nuclear.tooltip.absorption_factor",
                prop.absorptionFactor.toInt()
            )
        } else {
            this.maxTemp = Component.empty()
            this.moderationFactor = Component.empty()
            this.absorptionFactor = Component.empty()
        }
    }

    fun drawInfo(graphics: GuiGraphics, minecraft: Minecraft) {
        val font = minecraft.font
        val start = 40
        val lineHeight = font.lineHeight + 1
        val color = -0xeeeeef

        graphics.drawString(font, maxTemp, 0, start, color, false)
        graphics.drawString(font, moderationFactor, 0, start + lineHeight, color, false)
        graphics.drawString(font, absorptionFactor, 0, start + 2 * lineHeight, color, false)
    }
}
