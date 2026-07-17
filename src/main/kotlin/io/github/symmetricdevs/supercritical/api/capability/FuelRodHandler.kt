package io.github.symmetricdevs.supercritical.api.capability

import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandlerModifiable
import io.github.symmetricdevs.supercritical.api.machine.trait.LockableItemStackHandler
import io.github.symmetricdevs.supercritical.api.fission.stats.FissionFuelStats

interface FuelRodHandler : LockableHandler<ItemStack> {

    var fuel: FissionFuelStats?

    val partialFuel: FissionFuelStats?

    val depletionPoint: Double

    val depletedFuel: ItemStack

    /**
     * @return true if the partial fuel changed.
     */
    fun setPartialFuel(prop: FissionFuelStats?): Boolean

    /**
     * Binds the ECS fuel-rod [entity] backing this hatch (read live for weight / thermal-proportion
     * via the reactor world), or `null` to detach it.
     */
    fun bindFuelRodEntity(entity: io.github.symmetricdevs.supercritical.api.fission.ecs.Entity?)


    fun isDepleted(totalDepletion: Double): Boolean

    fun markUndepleted()

    val inputStackHandler: LockableItemStackHandler?

    fun getOutputStackHandler(depth: Int): IItemHandlerModifiable?

    fun resetDepletion(fuelDepletion: Double)

}
