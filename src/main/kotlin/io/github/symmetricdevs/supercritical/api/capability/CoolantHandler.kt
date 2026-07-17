package io.github.symmetricdevs.supercritical.api.capability

import io.github.symmetricdevs.supercritical.api.machine.multiblock.FissionReactorHatch
import io.github.symmetricdevs.supercritical.api.machine.trait.LockableFluidTank
import net.minecraft.core.Direction
import net.minecraftforge.fluids.FluidStack

interface CoolantHandler : LockableHandler<FluidStack>, FissionReactorHatch {

    val fluidTank: LockableFluidTank

    val coolantFrontFacing: Direction

    val outputHandler: CoolantHandler?
}
