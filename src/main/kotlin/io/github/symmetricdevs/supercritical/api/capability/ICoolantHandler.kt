package io.github.symmetricdevs.supercritical.api.capability

import io.github.symmetricdevs.supercritical.api.machine.multiblock.IFissionReactorHatch
import io.github.symmetricdevs.supercritical.api.machine.trait.LockableFluidTank
import net.minecraft.core.Direction
import net.minecraftforge.fluids.FluidStack

interface ICoolantHandler : ILockableHandler<FluidStack>, IFissionReactorHatch {

    val fluidTank: LockableFluidTank

    val coolantFrontFacing: Direction

    val outputHandler: ICoolantHandler?
}
