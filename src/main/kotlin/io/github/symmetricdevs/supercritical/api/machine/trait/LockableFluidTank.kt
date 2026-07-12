package io.github.symmetricdevs.supercritical.api.machine.trait

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

class LockableFluidTank(machine: MetaMachine, capacity: Int, io: IO?, capabilityIO: IO? = io) :
    NotifiableFluidTank(machine, 1, capacity, io, capabilityIO) {
    @Persisted(key = "locked")
    @DescSynced
    private var isLockedInternal = false

    @Persisted(key = "lockedFluid")
    @DescSynced
    private var lockedFluidStack: FluidStack = FluidStack.EMPTY

    var lockedState: Boolean
        get() = isLockedInternal
        set(value) {
            isLockedInternal = value
            if (!value) {
                lockedFluidStack = FluidStack.EMPTY
            } else if (!getFluidInTank(0).isEmpty) {
                lockedFluidStack = getFluidInTank(0).copy()
                lockedFluidStack.amount = 1
            }
        }

    val lockedObject: Fluid?
        get() = if (lockedFluidStack.isEmpty) null else lockedFluidStack.fluid

    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }


    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction?): Int {
        if (lockedState && !lockedFluidStack.isEmpty && resource.fluid !== lockedFluidStack.fluid) {
            return 0
        }
        val filled = super.fill(resource, action)
        if (lockedState && action == IFluidHandler.FluidAction.EXECUTE
            && lockedFluidStack.isEmpty && getFluidInTank(0).amount > 0
        ) {
            lockedFluidStack = getFluidInTank(0).copy()
            lockedFluidStack.amount = 1
        }
        return filled
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        if (lockedState && !lockedFluidStack.isEmpty && stack.fluid !== lockedFluidStack.fluid) {
            return false
        }
        return super.isFluidValid(tank, stack)
    }

    companion object {
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            LockableFluidTank::class.java, NotifiableFluidTank.MANAGED_FIELD_HOLDER
        )
    }
}