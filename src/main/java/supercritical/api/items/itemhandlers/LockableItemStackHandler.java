package supercritical.api.items.itemhandlers;

import net.minecraft.item.ItemStack;

import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import lombok.Getter;
import supercritical.api.capability.ILockableHandler;

public class LockableItemStackHandler extends NotifiableItemStackHandler implements ILockableHandler<ItemStack> {

    @Getter
    protected boolean locked;
    protected ItemStack lockedItemStack = ItemStack.EMPTY;

    public LockableItemStackHandler(MetaTileEntity entityToNotify, boolean isExport) {
        super(entityToNotify, 1, entityToNotify, isExport);
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
        if (this.locked && !this.lockedItemStack.isItemEqual(stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack getLockedObject() {
        return lockedItemStack;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound comp =  super.serializeNBT();
        comp.setBoolean("locked", locked);
        comp.setTag("locked_stack", this.lockedItemStack.serializeNBT());
        return comp;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        this.locked = nbt.getBoolean("locked");
        this.lockedItemStack = new ItemStack(nbt.getCompoundTag("locked_stack"));
    }
}
