package io.github.symmetricdevs.supercritical.api.capability

import net.minecraft.core.Direction
import net.minecraft.world.level.material.Fluid
import io.github.symmetricdevs.supercritical.api.machine.multiblock.IFissionReactorHatch
import io.github.symmetricdevs.supercritical.api.machine.trait.LockableFluidTank

interface ICoolantHandler : ILockableHandler<Fluid?>, IFissionReactorHatch {

    val fluidTank: LockableFluidTank

    val coolantFrontFacing: Direction

    val outputHandler: ICoolantHandler?
}
