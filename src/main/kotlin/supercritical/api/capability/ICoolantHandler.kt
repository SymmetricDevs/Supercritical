package supercritical.api.capability

import net.minecraft.world.level.material.Fluid
import supercritical.api.capability.impl.LockableFluidTank
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch
import supercritical.api.nuclear.fission.ICoolantStats

interface ICoolantHandler : ILockableHandler<Fluid?>, IFissionReactorHatch {
    override fun isLocked(): Boolean

    override fun setLock(isLocked: Boolean)

    var coolant: ICoolantStats?

    val fluidTank: LockableFluidTank

    val frontFacing: Direction

    val outputHandler: ICoolantHandler?
}
