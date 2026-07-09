package supercritical.common.metatileentities.multi.multiblockpart

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch
import supercritical.common.registry.SCBlocks

class MetaTileEntityControlRodPort(holder: IMachineBlockEntity, tier: Int, private val hasModeratorTip: Boolean) :
    TieredPartMachine(holder, tier), IFissionReactorHatch {
    fun hasModeratorTip(): Boolean {
        return hasModeratorTip
    }

    override fun checkValidity(depth: Int): Boolean {
        val pos = getPos()!!.mutable()
        val back = getFrontFacing().getOpposite()
        for (i in 1..<depth) {
            pos.move(back)
            if (getLevel()!!.getBlockState(pos).getBlock() !== SCBlocks.CONTROL_ROD_CHANNEL.get()) {
                return false
            }
        }
        pos.move(back)
        return getLevel()!!.getBlockState(pos).getBlock() === SCBlocks.REACTOR_VESSEL.get()
    }
}
