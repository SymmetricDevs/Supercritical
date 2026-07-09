package supercritical.api.items.itemhandlers;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import supercritical.api.capability.ILockableHandler;

public class LockableItemStackHandler extends NotifiableItemStackHandler implements ILockableHandler<ItemStack> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            LockableItemStackHandler.class, NotifiableItemStackHandler.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    protected boolean locked;
    @Persisted
    @DescSynced
    protected ItemStack lockedItemStack = ItemStack.EMPTY;

    public LockableItemStackHandler(MetaMachine machine, IO io) {
        super(machine, 1, io, io.support(IO.IN) ? IO.BOTH : io);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setLock(boolean isLocked) {
        this.locked = isLocked;
        if (isLocked && !this.getStackInSlot(0).isEmpty()) {
            lockedItemStack = this.getStackInSlot(0).copy();
            lockedItemStack.setCount(1);
        } else {
            lockedItemStack = ItemStack.EMPTY;
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (this.locked && !this.lockedItemStack.isEmpty() && !ItemStack.isSameItem(this.lockedItemStack, stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (this.locked && !this.lockedItemStack.isEmpty() && !ItemStack.isSameItem(this.lockedItemStack, stack)) {
            return false;
        }
        return super.isItemValid(slot, stack);
    }

    @Override
    public ItemStack getLockedObject() {
        return lockedItemStack;
    }
}
