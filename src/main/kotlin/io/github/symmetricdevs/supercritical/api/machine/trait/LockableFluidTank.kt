package io.github.symmetricdevs.supercritical.api.machine.trait

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import io.github.symmetricdevs.supercritical.api.capability.ILockableHandler
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

/**
 * Single-tank [NotifiableFluidTank] that surfaces a content-type lock as [ILockableHandler].
 *
 * Hybrid of SC's lock *intent* and GTCEu's embedded lock, because the two can't collapse into one:
 * [io.github.symmetricdevs.supercritical.common.machine.multiblock.part.CoolantExportHatch] is
 * locked while its tank is still empty (hot coolant is only produced once the reactor runs), and
 * GTCEu's `isLocked` — derived from a non-empty `lockedFluid` — cannot represent that state.
 *
 * [lockIntent] is the persisted intent (the source of truth for [ILockableHandler] and the UI
 * label). It is deliberately named `lockIntent` rather than `locked` so the property can be
 * field-backed without its setter's JVM signature (`setLocked(Z)V`) colliding with the inherited
 * [NotifiableFluidTank.setLocked] — a property literally named `locked` triggers an accidental
 * override there. GTCEu's own `isLocked`/`setLocked` keep tracking the embedded `lockedFluid`, so
 * the [lockIntent] setter installs/clears that sample + filter directly via [installLock]/
 * [clearLock], and [fill] performs the deferred capture for the locked-but-empty case. The coolant
 * hatches persist/restore `lockIntent` via their NBT ("Locked"), which re-runs the setter and
 * re-derives the sample on reload.
 */
class LockableFluidTank(machine: MetaMachine, capacity: Int, io: IO?, capabilityIO: IO? = io) :
    NotifiableFluidTank(machine, 1, capacity, io, capabilityIO), ILockableHandler<FluidStack> {

    @Persisted
    @DescSynced
    override var lockIntent: Boolean = false
        set(value) {
            field = value
            if (value) {
                val current = getFluidInTank(0)
                // CoolantExportHatch locks while empty — defer sample capture to the first fill().
                if (!current.isEmpty) installLock(current)
            } else {
                clearLock()
            }
        }

    override val stack: FluidStack
        get() = getLockedFluid().fluid

    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction?): Int {
        val filled = super.fill(resource, action)
        // Deferred capture for the locked-but-empty case: install the embedded lock against the
        // first fluid that lands so the filter takes effect for subsequent fills.
        if (lockIntent && getLockedFluid().fluid.isEmpty && action == IFluidHandler.FluidAction.EXECUTE &&
            filled > 0 && !getFluidInTank(0).isEmpty
        ) {
            installLock(getFluidInTank(0))
        }
        return filled
    }

    /** Write the locked sample + fluid-equality filter into GTCEu's embedded `lockedFluid`. */
    private fun installLock(sample: FluidStack) {
        val lockedSample = sample.copy().also { it.amount = 1 }
        getLockedFluid().setFluid(lockedSample)
        setFilter { fluid -> fluid.isFluidEqual(lockedSample) }
    }

    /** Clear the embedded sample and reset the filter to accept anything. */
    private fun clearLock() {
        getLockedFluid().setFluid(FluidStack.EMPTY)
        setFilter { true }
    }

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    companion object {
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            LockableFluidTank::class.java, NotifiableFluidTank.MANAGED_FIELD_HOLDER
        )
    }
}
