package supercritical.common.metatileentities.multi.multiblockpart

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine
import net.minecraft.core.BlockPos
import supercritical.api.machine.multiblock.IFissionReactorHatch
import supercritical.common.registry.SCBlocks

class MetaTileEntityControlRodPort(holder: IMachineBlockEntity, tier: Int, private val hasModeratorTip: Boolean) :
    TieredPartMachine(holder, tier), IFissionReactorHatch {
    override val hatchPos: BlockPos?
        get() = pos

    fun hasModeratorTip(): Boolean {
        return hasModeratorTip
    }

    override fun checkValidity(depth: Int): Boolean {
        val level = level ?: return false
        val pos = (pos ?: return false).mutable()
        val back = frontFacing.opposite
        for (i in 1..<depth) {
            pos.move(back)
            if (level.getBlockState(pos).block !== SCBlocks.CONTROL_ROD_CHANNEL.get()) {
                return false
            }
        }
        pos.move(back)
        return level.getBlockState(pos).block === SCBlocks.REACTOR_VESSEL.get()
    }
}
