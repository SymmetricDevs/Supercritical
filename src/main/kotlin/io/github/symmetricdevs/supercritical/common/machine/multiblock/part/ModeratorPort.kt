package io.github.symmetricdevs.supercritical.common.machine.multiblock.part

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine
import net.minecraft.core.BlockPos
import io.github.symmetricdevs.supercritical.api.machine.multiblock.FissionReactorHatch
import io.github.symmetricdevs.supercritical.api.fission.stats.ModeratorStats
import io.github.symmetricdevs.supercritical.common.data.ScritBlocks

class ModeratorPort(holder: IMachineBlockEntity, tier: Int) : TieredPartMachine(holder, tier),
    FissionReactorHatch {
    override val hatchPos: BlockPos?
        get() = pos

    var moderator: ModeratorStats? = null
        private set

    override fun checkValidity(depth: Int): Boolean {
        val level = level ?: return false
        val pos = (pos ?: return false).mutable()
        val back = frontFacing.opposite
        val defaultState = level.getBlockState(pos.move(back))
        val stats = ModeratorStats.of(defaultState.block)
        this.moderator = stats
        if (stats == null) return false
        // Legacy semantics: exactly one moderator block sits directly behind the port; the rest of
        // the column is ordinary reactor interior, then the far vessel wall. So a middle block that
        // IS the moderator is invalid (do not require a solid moderator column). Mirrors legacy
        // MetaTileEntityModeratorPort.checkValidity.
        for (i in 2..<depth) {
            pos.move(back)
            if (level.getBlockState(pos) == defaultState) {
                return false
            }
        }
        pos.move(back)
        return level.getBlockState(pos).block === ScritBlocks.REACTOR_VESSEL.get()
    }
}
