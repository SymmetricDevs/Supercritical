package supercritical.api.capability.impl

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import supercritical.api.capability.ILockableHandler

class LockableFluidTank(machine: MetaMachine, capacity: Int, io: IO?) : NotifiableFluidTank(machine, 1, capacity, io),
    ILockableHandler<Fluid?> {
    @Persisted
    @DescSynced
    private var locked = false

    @Persisted
    @DescSynced
    private var lockedFluid: FluidStack = FluidStack.EMPTY

    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    override fun getLockedObject(): Fluid? {
        return if (lockedFluid.isEmpty()) null else lockedFluid.getFluid()
    }

    override fun setLock(isLocked: Boolean) {
        this.locked = isLocked
        if (!isLocked) {
            this.lockedFluid = FluidStack.EMPTY
        } else if (!getFluidInTank(0).isEmpty()) {
            this.lockedFluid = getFluidInTank(0).copy()
            this.lockedFluid.setAmount(1)
        }
    }

    override fun fill(resource: FluidStack, action: FluidAction?): Int {
        if (locked && !lockedFluid.isEmpty() && resource.getFluid() !== lockedFluid.getFluid()) {
            return 0
        }
        return super.fill(resource, action)
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        if (locked && !lockedFluid.isEmpty() && stack.getFluid() !== lockedFluid.getFluid()) {
            return false
        }
        return super.isFluidValid(tank, stack)
    }

    override fun isLocked(): Boolean {
        return locked
    }

    companion object {
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            LockableFluidTank::class.java, NotifiableFluidTank.MANAGED_FIELD_HOLDER
        )
    }
}
