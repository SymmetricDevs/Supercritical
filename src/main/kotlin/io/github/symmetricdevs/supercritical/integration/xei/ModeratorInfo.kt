package io.github.symmetricdevs.supercritical.integration.xei

import io.github.symmetricdevs.supercritical.api.fission.stats.ModeratorStats
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

/**
 * Viewer-neutral data wrapper for a moderator block entry, shared by JEI/REI/EMI.
 */
class ModeratorInfo(block: Block) {
    val blockState: BlockState = block.defaultBlockState()
    val stack: ItemStack = ItemStack(block)

    val textLines: List<Component>

    init {
        val prop = ModeratorStats.of(block)
        textLines = if (prop != null) listOf(
            Component.translatable("metaitem.nuclear.tooltip.temperature", prop.maxTemperature),
            Component.translatable(
                "metaitem.nuclear.tooltip.moderation_factor",
                prop.moderationFactor.toInt()
            ),
            Component.translatable(
                "metaitem.nuclear.tooltip.absorption_factor",
                prop.absorptionFactor.toInt()
            )
        ) else emptyList()
    }
}
