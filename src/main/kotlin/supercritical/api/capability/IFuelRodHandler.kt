package supercritical.api.capability

import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandlerModifiable
import supercritical.api.items.itemhandlers.LockableItemStackHandler
import supercritical.api.nuclear.fission.IFissionFuelStats
import supercritical.api.nuclear.fission.components.FuelRod

interface IFuelRodHandler : ILockableHandler<ItemStack?> {
    var fuel: IFissionFuelStats?

    val partialFuel: IFissionFuelStats?

    /**
     * @return true if the partial fuel changed.
     */
    fun setPartialFuel(prop: IFissionFuelStats?): Boolean

    fun setInternalFuelRod(rod: FuelRod?)

    val depletionPoint: Double

    fun isDepleted(totalDepletion: Double): Boolean

    fun markUndepleted()

    val inputStackHandler: LockableItemStackHandler?

    fun getOutputStackHandler(depth: Int): IItemHandlerModifiable?

    fun resetDepletion(fuelDepletion: Double)

    val depletedFuel: ItemStack?
}
