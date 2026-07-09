package supercritical.api.capability.impl;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import supercritical.api.capability.ILockableHandler;

public class LockableFluidTank extends NotifiableFluidTank implements ILockableHandler<Fluid> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            LockableFluidTank.class, NotifiableFluidTank.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private boolean locked;
    @Persisted
    @DescSynced
    private FluidStack lockedFluid = FluidStack.EMPTY;

    public LockableFluidTank(MetaMachine machine, int capacity, IO io) {
        super(machine, 1, capacity, io);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public Fluid getLockedObject() {
        return lockedFluid.isEmpty() ? null : lockedFluid.getFluid();
    }

    @Override
    public void setLock(boolean isLocked) {
        this.locked = isLocked;
        if (!isLocked) {
            this.lockedFluid = FluidStack.EMPTY;
        } else if (!getFluidInTank(0).isEmpty()) {
            this.lockedFluid = getFluidInTank(0).copy();
            this.lockedFluid.setAmount(1);
        }
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (locked && !lockedFluid.isEmpty() && resource.getFluid() != lockedFluid.getFluid()) {
            return 0;
        }
        return super.fill(resource, action);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (locked && !lockedFluid.isEmpty() && stack.getFluid() != lockedFluid.getFluid()) {
            return false;
        }
        return super.isFluidValid(tank, stack);
    }

    @Override
    public boolean isLocked() {
        return locked;
    }
}
