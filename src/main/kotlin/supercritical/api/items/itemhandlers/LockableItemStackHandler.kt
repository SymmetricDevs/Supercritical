package supercritical.api.items.itemhandlers

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.world.item.ItemStack
import supercritical.api.capability.ILockableHandler

class LockableItemStackHandler(machine: MetaMachine, io: IO) : NotifiableItemStackHandler(
    machine, 1, io, if (io.support(
            IO.IN
        )
    ) IO.BOTH else io
), ILockableHandler<ItemStack?> {
    @Persisted
    @DescSynced
    protected var locked: Boolean = false

    @Persisted
    @DescSynced
    protected var lockedItemStack: ItemStack = ItemStack.EMPTY

    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    override fun isLocked(): Boolean {
        return locked
    }

    override fun setLock(isLocked: Boolean) {
        this.locked = isLocked
        if (isLocked && !this.getStackInSlot(0).isEmpty()) {
            lockedItemStack = this.getStackInSlot(0).copy()
            lockedItemStack.setCount(1)
        } else {
            lockedItemStack = ItemStack.EMPTY
        }
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (this.locked && !this.lockedItemStack.isEmpty() && !ItemStack.isSameItem(this.lockedItemStack, stack)) {
            return stack
        }
        return super.insertItem(slot, stack, simulate)
    }

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        if (this.locked && !this.lockedItemStack.isEmpty() && !ItemStack.isSameItem(this.lockedItemStack, stack)) {
            return false
        }
        return super.isItemValid(slot, stack)
    }

    override fun getLockedObject(): ItemStack {
        return lockedItemStack
    }

    companion object {
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            LockableItemStackHandler::class.java, NotifiableItemStackHandler.MANAGED_FIELD_HOLDER
        )
    }
}
