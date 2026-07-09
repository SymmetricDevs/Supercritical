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
import supercritical.common.registry.SCBlocks

class MetaTileEntityCoolantImportHatch(holder: IMachineBlockEntity, tier: Int) :
    TieredIOPartMachine(holder, tier, IO.IN), ICoolantHandler, IControllable, IFissionReactorHatch {
    @Persisted
    @DescSynced
    @RequireRerender
    private var workingEnabled = true
    private val fluidTank: LockableFluidTank
    private var coolant: ICoolantStats? = null
    private var pairedHatch: MetaTileEntityCoolantExportHatch? = null

    init {
        this.fluidTank = LockableFluidTank(this, 16000, IO.IN)
    }

    override fun isWorkingEnabled(): Boolean {
        return workingEnabled
    }

    override fun setWorkingEnabled(workingEnabled: Boolean) {
        this.workingEnabled = workingEnabled
    }

    override fun checkValidity(depth: Int): Boolean {
        this.pairedHatch = getExportHatch(depth)
        return pairedHatch != null
    }

    fun getExportHatch(depth: Int): MetaTileEntityCoolantExportHatch? {
        val pos = getPos()!!.mutable()
        val back = getFrontFacing().getOpposite()
        for (i in 1..<depth) {
            pos.move(back)
            if (getLevel()!!.getBlockState(pos).getBlock() !== SCBlocks.COOLANT_CHANNEL.get()) {
                return null
            }
        }
        pos.move(back)
        if (getMachine(getLevel(), pos) is MetaTileEntityCoolantExportHatch) {
            return export
        }
        return null
    }

    override fun setLocked(isLocked: Boolean) {
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
        return pairedHatch
    }

    override fun onLoad() {
        super.onLoad()
        subscribeServerTick(Runnable {
            if (getOffsetTimer() % 5 == 0L && isWorkingEnabled()) {
                fluidTank.importFromNearby(getFrontFacing())
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
