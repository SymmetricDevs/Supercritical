package supercritical.common.metatileentities.multi.multiblockpart

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import supercritical.api.capability.IFuelRodHandler
import supercritical.api.items.itemhandlers.LockableItemStackHandler
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch
import supercritical.api.nuclear.fission.FissionFuelRegistry
import supercritical.api.nuclear.fission.IFissionFuelStats
import supercritical.api.nuclear.fission.components.FuelRod
import supercritical.common.metatileentities.multi.MetaTileEntityFissionReactor
import supercritical.common.registry.SCBlocks

class MetaTileEntityFuelRodImportBus(holder: IMachineBlockEntity, tier: Int) : TieredIOPartMachine(holder, tier, IO.IN),
    IFuelRodHandler, IControllable, IFissionReactorHatch {
    @Persisted
    @DescSynced
    @RequireRerender
    private var workingEnabled = true
    private val controller: MetaTileEntityFissionReactor? = null
    private var fuelProperty: IFissionFuelStats? = null
    private var pairedHatch: MetaTileEntityFuelRodExportBus? = null
    private var partialFuel: IFissionFuelStats? = null
    private var internalFuelRod: FuelRod? = null
    private var depletionPoint = 0.0

    private val lockableInventory: LockableItemStackHandler

    init {
        this.lockableInventory = LockableItemStackHandler(this, IO.IN)
    }

    val inventory: NotifiableItemStackHandler
        get() = lockableInventory

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

    fun getExportHatch(depth: Int): MetaTileEntityFuelRodExportBus? {
        val pos = getPos()!!.mutable()
        val back = getFrontFacing().getOpposite()
        for (i in 1..<depth) {
            pos.move(back)
            if (getLevel()!!.getBlockState(pos).getBlock() !== SCBlocks.FUEL_CHANNEL.get()) {
                return null
            }
        }
        pos.move(back)
        if (getMachine(getLevel(), pos) is MetaTileEntityFuelRodExportBus) {
            return export
        }
        return null
    }

    override fun setLock(isLocked: Boolean) {
        if (depletionPoint == 0.0) {
            lockableInventory.setLock(isLocked)
        }
    }

    override fun isLocked(): Boolean {
        return lockableInventory.isLocked()
    }

    override fun getLockedObject(): ItemStack? {
        return lockableInventory.getLockedObject()
    }

    override fun getFuel(): IFissionFuelStats? {
        return this.fuelProperty
    }

    override fun setFuel(prop: IFissionFuelStats?) {
        this.fuelProperty = prop
    }

    override fun getPartialFuel(): IFissionFuelStats? {
        return this.partialFuel
    }

    override fun setPartialFuel(prop: IFissionFuelStats?): Boolean {
        if (prop === this.partialFuel) return false
        this.partialFuel = prop
        if (prop == null) {
            this.internalFuelRod = null
        } else if (this.internalFuelRod != null) {
            this.internalFuelRod!!.setFuel(prop)
        }
        return true
    }

    override fun setInternalFuelRod(rod: FuelRod?) {
        this.internalFuelRod = rod
    }

    override fun isDepleted(totalDepletion: Double): Boolean {
        return this.depletionPoint <= totalDepletion * this.internalFuelRod!!.getWeight()
    }

    override fun markUndepleted() {
        if (this.partialFuel != null) {
            this.depletionPoint += this.partialFuel!!.getDuration().toDouble()
        }
    }

    override fun getInputStackHandler(): LockableItemStackHandler {
        return this.lockableInventory
    }

    override fun getOutputStackHandler(depth: Int): NotifiableItemStackHandler? {
        val export = getExportHatch(depth)
        return if (export == null) null else export.getInventory()
    }

    override fun resetDepletion(fuelDepletion: Double) {
        if (this.internalFuelRod == null) return
        this.depletionPoint -= fuelDepletion * this.internalFuelRod!!.getWeight()
    }

    override fun getDepletedFuel(): ItemStack? {
        if (this.internalFuelRod == null) return ItemStack.EMPTY
        return this.internalFuelRod!!.getDepletedFuel()
    }

    override fun getDepletionPoint(): Double {
        return this.depletionPoint
    }

    val currentDepletionRatio: Double
        get() {
            if (this.partialFuel == null) return 0.0
            val controller = getController()
            if (controller == null || !controller.isLocked() || controller.getReactor() == null) {
                return 1 - (depletionPoint / partialFuel!!.getDuration())
            }
            return 1 - ((depletionPoint - (controller.getReactor().fuelDepletion * internalFuelRod!!.getWeight()))
                    / partialFuel!!.getDuration())
        }

    fun voidPartialFuel() {
        val controller = getController()
        if (controller != null && controller.isLocked()) return
        setPartialFuel(null)
        depletionPoint = 0.0
        setLock(false)
    }

    override fun getController(): MetaTileEntityFissionReactor? {
        val controllers = getControllers()
        if (controllers.isEmpty()) return null
        if (controllers.first() is MetaTileEntityFissionReactor) {
            return reactor
        }
        return null
    }

    override fun onLoad() {
        super.onLoad()
        subscribeServerTick(Runnable {
            if (getOffsetTimer() % 5 == 0L && isWorkingEnabled()) {
                lockableInventory.importFromNearby(getFrontFacing())
            }
        })
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        tag.putBoolean("Locked", lockableInventory.isLocked())
        if (partialFuel != null) tag.putString("PartialFuel", partialFuel!!.getId())
        tag.putDouble("DepletionPoint", depletionPoint)
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        lockableInventory.setLock(tag.getBoolean("Locked"))
        if (tag.contains("PartialFuel")) {
            this.partialFuel = FissionFuelRegistry.getFissionFuel(tag.getString("PartialFuel"))
        }
        this.depletionPoint = tag.getDouble("DepletionPoint")
    }
}
