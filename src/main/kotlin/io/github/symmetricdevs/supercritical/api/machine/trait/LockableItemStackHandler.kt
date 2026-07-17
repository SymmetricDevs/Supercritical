package io.github.symmetricdevs.supercritical.api.machine.trait

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import io.github.symmetricdevs.supercritical.api.capability.LockableHandler
import net.minecraft.world.item.ItemStack

open class LockableItemStackHandler(machine: MetaMachine, io: IO) : NotifiableItemStackHandler(
    machine, 1, io, if (io.support(
            IO.IN
        )
    ) IO.BOTH else io
), LockableHandler<ItemStack> {

    @Persisted
    @DescSynced
    override var lockIntent: Boolean = false
        set(lockIntent) {
            field = lockIntent
            if (lockIntent && !getStackInSlot(0).isEmpty) {
                stack = getStackInSlot(0).copy()
                stack.setCount(1)
            } else {
                stack = ItemStack.EMPTY
            }
        }

    @Persisted
    @DescSynced
    override var stack: ItemStack = ItemStack.EMPTY


    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (lockIntent && !ItemStack.isSameItem(this@LockableItemStackHandler.stack, stack)) {
            return stack
        }
        return super.insertItem(slot, stack, simulate)
    }

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        if (lockIntent && !ItemStack.isSameItem(this@LockableItemStackHandler.stack, stack)) {
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
