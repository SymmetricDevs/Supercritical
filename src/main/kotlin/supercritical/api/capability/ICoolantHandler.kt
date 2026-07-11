package supercritical.api.capability

import net.minecraft.core.Direction
import net.minecraft.world.level.material.Fluid
import supercritical.api.machine.multiblock.IFissionReactorHatch
import supercritical.api.machine.trait.LockableFluidTank
import supercritical.api.nuclear.fission.ICoolantStats

interface ICoolantHandler : ILockableHandler<Fluid?>, IFissionReactorHatch {

    var coolant: ICoolantStats?

    val fluidTank: LockableFluidTank

    val coolantFrontFacing: Direction

    val outputHandler: ICoolantHandler?
}
