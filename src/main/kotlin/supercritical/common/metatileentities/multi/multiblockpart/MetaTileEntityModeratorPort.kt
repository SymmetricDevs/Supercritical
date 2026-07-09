package supercritical.common.metatileentities.multi.multiblockpart

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch
import supercritical.api.nuclear.fission.IModeratorStats
import supercritical.api.nuclear.fission.ModeratorRegistry
import supercritical.common.registry.SCBlocks

class MetaTileEntityModeratorPort(holder: IMachineBlockEntity, tier: Int) : TieredPartMachine(holder, tier),
    IFissionReactorHatch {
    var moderator: IModeratorStats? = null
        private set

    override fun checkValidity(depth: Int): Boolean {
        val pos = getPos()!!.mutable()
        val back = getFrontFacing().getOpposite()
        val defaultState = getLevel()!!.getBlockState(pos.move(back))
        val stats = ModeratorRegistry.getModerator(defaultState.getBlock())
        this.moderator = stats
        if (stats == null) return false
        for (i in 2..<depth) {
            pos.move(back)
            if (getLevel()!!.getBlockState(pos) != defaultState) {
                return false
            }
        }
        pos.move(back)
        return getLevel()!!.getBlockState(pos).getBlock() === SCBlocks.REACTOR_VESSEL.get()
    }
}
