package io.github.symmetricdevs.supercritical.api.fission.stats

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import io.github.symmetricdevs.supercritical.common.data.ScritPropertyKeys
import net.minecraft.world.level.block.Block

interface ModeratorStats {
    val maxTemperature: Double

    val moderationFactor: Double

    val absorptionFactor: Double

    companion object {
        /** The moderator stats for [block], or null if the block's material has no moderator property. */
        fun of(block: Block): ModeratorStats? {
            val stack = ChemicalHelper.getMaterialStack(block)
            if (stack.isEmpty) return null
            val material = stack.material()
            if (!material.hasProperty(ScritPropertyKeys.MODERATOR)) return null
            return material.getProperty(ScritPropertyKeys.MODERATOR)
        }
    }
}
