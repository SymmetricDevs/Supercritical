package supercritical.common.metatileentities.multi.multiblockpart

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch

class MetaTileEntityFuelRodExportBus(holder: IMachineBlockEntity, tier: Int) :
    TieredIOPartMachine(holder, tier, IO.OUT), IControllable, IFissionReactorHatch {
    @Persisted
    @DescSynced
    @RequireRerender
    private var workingEnabled = true
    val inventory: NotifiableItemStackHandler

    init {
        this.inventory = NotifiableItemStackHandler(this, 1, IO.OUT)
    }

    override fun isWorkingEnabled(): Boolean {
        return workingEnabled
    }

    override fun setWorkingEnabled(workingEnabled: Boolean) {
        this.workingEnabled = workingEnabled
    }

    override fun checkValidity(depth: Int): Boolean {
        return true
    }

    override fun onLoad() {
        super.onLoad()
        subscribeServerTick(Runnable {
            if (getOffsetTimer() % 5 == 0L && isWorkingEnabled()) {
                inventory.exportToNearby(getFrontFacing())
            }
        })
    }
}
