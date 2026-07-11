package supercritical.common.metatileentities.multi.multiblockpart

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.Fluid
import supercritical.api.capability.ICoolantHandler
import supercritical.api.capability.impl.LockableFluidTank
import supercritical.api.machine.multiblock.IFissionReactorHatch
import supercritical.api.nuclear.fission.ICoolantStats

class MetaTileEntityCoolantExportHatch(holder: IMachineBlockEntity, tier: Int) :
    TieredIOPartMachine(holder, tier, IO.OUT), ICoolantHandler, IControllable, IFissionReactorHatch {
    override val fluidTank: LockableFluidTank
    override var coolant: ICoolantStats? = null

    init {
        this.fluidTank = LockableFluidTank(this, 16000, IO.OUT)
    }

    override val coolantFrontFacing: Direction
        get() = frontFacing

    override val hatchPos
        get() = pos

    override var locked: Boolean
        get() = fluidTank.lockedState
        set(value) {
            fluidTank.lockedState = value
        }

    override val lockedObject: Fluid?
        get() = fluidTank.lockedObject

    override val outputHandler: ICoolantHandler
        get() = this


    override fun checkValidity(depth: Int): Boolean {
        return true
    }

    override fun onLoad() {
        super.onLoad()
        subscribeServerTick {
            if (offsetTimer % 5 == 0L && isWorkingEnabled) {
                fluidTank.exportToNearby(frontFacing)
            }
        }
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        tag.putBoolean("Locked", fluidTank.lockedState)
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        fluidTank.lockedState = tag.getBoolean("Locked")
    }
}
