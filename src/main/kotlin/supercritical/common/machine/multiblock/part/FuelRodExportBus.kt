package supercritical.common.machine.multiblock.part

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import net.minecraft.core.BlockPos
import supercritical.api.machine.multiblock.IFissionReactorHatch

class FuelRodExportBus(holder: IMachineBlockEntity, tier: Int) :
    TieredIOPartMachine(holder, tier, IO.OUT), IControllable, IFissionReactorHatch {
    val inventory: NotifiableItemStackHandler = NotifiableItemStackHandler(this, 1, IO.OUT)

    override val hatchPos: BlockPos?
        get() = pos

    override fun checkValidity(depth: Int): Boolean {
        return true
    }

    override fun onLoad() {
        super.onLoad()
        subscribeServerTick {
            if (offsetTimer % 5 == 0L && isWorkingEnabled) {
                inventory.exportToNearby(frontFacing)
            }
        }
    }

}
