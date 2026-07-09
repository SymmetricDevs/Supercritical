package supercritical.common.metatileentities.multi.multiblockpart

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.Fluid
import supercritical.api.capability.ICoolantHandler
import supercritical.api.capability.impl.LockableFluidTank
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch
import supercritical.api.nuclear.fission.ICoolantStats

class MetaTileEntityCoolantExportHatch(holder: IMachineBlockEntity, tier: Int) :
    TieredIOPartMachine(holder, tier, IO.OUT), ICoolantHandler, IControllable, IFissionReactorHatch {
    @Persisted
    @DescSynced
    @RequireRerender
    private var workingEnabled = true
    private val fluidTank: LockableFluidTank
    private var coolant: ICoolantStats? = null

    init {
        this.fluidTank = LockableFluidTank(this, 16000, IO.OUT)
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

    override fun setLock(isLocked: Boolean) {
        fluidTank.setLock(isLocked)
    }

    override fun isLocked(): Boolean {
        return fluidTank.isLocked()
    }

    override fun getLockedObject(): Fluid? {
        return fluidTank.getLockedObject()
    }

    override fun getCoolant(): ICoolantStats? {
        return this.coolant
    }

    override fun setCoolant(prop: ICoolantStats?) {
        this.coolant = prop
    }

    override fun getFluidTank(): LockableFluidTank {
        return this.fluidTank
    }

    override fun getOutputHandler(): ICoolantHandler? {
        return this
    }

    override fun onLoad() {
        super.onLoad()
        subscribeServerTick(Runnable {
            if (getOffsetTimer() % 5 == 0L && isWorkingEnabled()) {
                fluidTank.exportToNearby(getFrontFacing())
            }
        })
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        tag.putBoolean("Locked", fluidTank.isLocked())
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        fluidTank.setLock(tag.getBoolean("Locked"))
    }
}
