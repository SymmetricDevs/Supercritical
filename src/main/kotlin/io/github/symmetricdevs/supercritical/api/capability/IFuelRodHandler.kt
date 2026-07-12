package io.github.symmetricdevs.supercritical.api.capability

import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandlerModifiable
import io.github.symmetricdevs.supercritical.api.machine.trait.LockableItemStackHandler
import io.github.symmetricdevs.supercritical.api.nuclear.fission.IFissionFuelStats
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.FuelRod

interface IFuelRodHandler : ILockableHandler<ItemStack> {

    var fuel: IFissionFuelStats?

    val partialFuel: IFissionFuelStats?

    val depletionPoint: Double

    val depletedFuel: ItemStack

    /**
     * @return true if the partial fuel changed.
     */
    fun setPartialFuel(prop: IFissionFuelStats?): Boolean

    fun setInternalFuelRod(rod: FuelRod?)


    fun isDepleted(totalDepletion: Double): Boolean

    fun markUndepleted()

    val inputStackHandler: LockableItemStackHandler?

    fun getOutputStackHandler(depth: Int): IItemHandlerModifiable?

    fun resetDepletion(fuelDepletion: Double)

}
