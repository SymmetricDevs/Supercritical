package supercritical.api.items.itemhandlers;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import lombok.Getter;
import supercritical.api.capability.ILockableHandler;

public class LockableItemStackHandler extends NotifiableItemStackHandler implements ILockableHandler<ItemStack> {

    @Getter
    protected boolean locked;
    protected ItemStack lockedItemStack;

    public LockableItemStackHandler(MetaTileEntity entityToNotify, boolean isExport) {
        super(entityToNotify, 1, entityToNotify, isExport);
    }

    @Override
    public void setLock(boolean isLocked) {
        this.locked = isLocked;
        if (isLocked && !this.getStackInSlot(0).isEmpty()) {
            lockedItemStack = this.getStackInSlot(0).copy();
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (this.locked && !this.lockedItemStack.isItemEqual(stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack getLockedObject() {
        return lockedItemStack;
    }
}
