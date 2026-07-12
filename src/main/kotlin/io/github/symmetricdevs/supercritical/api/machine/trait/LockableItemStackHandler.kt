package io.github.symmetricdevs.supercritical.api.machine.trait

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.world.item.ItemStack
import io.github.symmetricdevs.supercritical.api.capability.ILockableHandler

open class LockableItemStackHandler(machine: MetaMachine, io: IO) : NotifiableItemStackHandler(
    machine, 1, io, if (io.support(
            IO.IN
        )
    ) IO.BOTH else io
), ILockableHandler<ItemStack> {

    @Persisted
    @DescSynced
    override var locked: Boolean = false
        set(locked) {
            field = locked
            if (locked && !getStackInSlot(0).isEmpty) {
                lockedObject = getStackInSlot(0).copy()
                lockedObject.setCount(1)
            } else {
                lockedObject = ItemStack.EMPTY
            }
        }

    @Persisted
    @DescSynced
    override var lockedObject: ItemStack = ItemStack.EMPTY


    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (locked && !ItemStack.isSameItem(lockedObject, stack)) {
            return stack
        }
        return super.insertItem(slot, stack, simulate)
    }

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        if (locked && !ItemStack.isSameItem(lockedObject, stack)) {
            return false
        }
        return super.isItemValid(slot, stack)
    }

    companion object {
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            LockableItemStackHandler::class.java, NotifiableItemStackHandler.MANAGED_FIELD_HOLDER
        )
    }
}
