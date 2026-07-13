package io.github.symmetricdevs.supercritical.api.capability

import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandlerModifiable
import io.github.symmetricdevs.supercritical.api.machine.trait.LockableItemStackHandler
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Entity
import io.github.symmetricdevs.supercritical.api.nuclear.fission.IFissionFuelStats

interface IFuelRodHandler : ILockableHandler<ItemStack> {

    var fuel: IFissionFuelStats?

    val partialFuel: IFissionFuelStats?

    val depletionPoint: Double

    val depletedFuel: ItemStack

    /**
     * @return true if the partial fuel changed.
     */
    fun setPartialFuel(prop: IFissionFuelStats?): Boolean

    /**
     * Binds the ECS fuel-rod [entity] backing this hatch (read live for weight / thermal-proportion
     * via the reactor world), or `null` to detach it.
     */
    fun bindFuelRodEntity(entity: Entity?)


    fun isDepleted(totalDepletion: Double): Boolean

    fun markUndepleted()

    val inputStackHandler: LockableItemStackHandler?

    fun getOutputStackHandler(depth: Int): IItemHandlerModifiable?

    fun resetDepletion(fuelDepletion: Double)

}
