package supercritical.common.machine.multiblock.multiblockpart

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.UITemplate
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.ItemStackHandler
import supercritical.api.capability.IFuelRodHandler
import supercritical.api.items.itemhandlers.LockableItemStackHandler
import supercritical.api.machine.multiblock.IFissionReactorHatch
import supercritical.api.nuclear.fission.FissionFuelRegistry
import supercritical.api.nuclear.fission.IFissionFuelStats
import supercritical.api.nuclear.fission.components.FuelRod
import supercritical.common.machine.multiblock.FissionReactor
import supercritical.common.registry.ScritBlocks

class FuelRodImportBus(holder: IMachineBlockEntity, tier: Int) : TieredIOPartMachine(holder, tier, IO.IN),
    IFuelRodHandler, IControllable, IFissionReactorHatch, IUIMachine {
    override var fuel: IFissionFuelStats? = null
    private var pairedHatch: FuelRodExportBus? = null
    override var partialFuel: IFissionFuelStats? = null
    private var internalFuelRod: FuelRod? = null
    override var depletionPoint = 0.0

    private val lockableInventory: LockableItemStackHandler = LockableItemStackHandler(this, IO.IN)

    /**
     * Display-only item handler whose slot mirrors the currently locked fuel rod, matching the
     * legacy partial-fuel display slot.
     */
    private val partialFuelDisplay: ItemStackHandler = object : ItemStackHandler(1) {
        override fun getStackInSlot(slot: Int): ItemStack = this@FuelRodImportBus.lockedObject
    }

    val inventory: NotifiableItemStackHandler
        get() = lockableInventory

    override val inputStackHandler: LockableItemStackHandler
        get() = lockableInventory

    override val depletedFuel: ItemStack
        get() = internalFuelRod?.depletedFuel ?: ItemStack.EMPTY

    override val hatchPos: BlockPos?
        get() = pos

    override var locked: Boolean
        get() = lockableInventory.locked
        set(value) {
            if (depletionPoint == 0.0) lockableInventory.locked = value
        }

    override val lockedObject: ItemStack
        get() = lockableInventory.lockedObject

    override fun checkValidity(depth: Int): Boolean {
        this.pairedHatch = getExportHatch(depth)
        return pairedHatch != null
    }

    fun getExportHatch(depth: Int): FuelRodExportBus? {
        val level = level ?: return null
        val pos = (pos ?: return null).mutable()
        val back = frontFacing.opposite
        for (i in 1..<depth) {
            pos.move(back)
            if (level.getBlockState(pos).block !== ScritBlocks.FUEL_CHANNEL.get()) {
                return null
            }
        }
        pos.move(back)
        return getMachine(level, pos) as? FuelRodExportBus
    }


    override fun setPartialFuel(prop: IFissionFuelStats?): Boolean {
        if (prop === this.partialFuel) return false
        this.partialFuel = prop
        if (prop == null) {
            this.internalFuelRod = null
        } else {
            this.internalFuelRod?.setFuel(prop)
        }
        return true
    }

    override fun setInternalFuelRod(rod: FuelRod?) {
        this.internalFuelRod = rod
    }

    override fun isDepleted(totalDepletion: Double): Boolean {
        return this.depletionPoint <= totalDepletion * this.internalFuelRod!!.weight
    }

    override fun markUndepleted() {
        val partial = this.partialFuel ?: return
        this.depletionPoint += partial.duration.toDouble()
    }

    override fun getOutputStackHandler(depth: Int): NotifiableItemStackHandler? {
        return getExportHatch(depth)?.inventory
    }

    override fun resetDepletion(fuelDepletion: Double) {
        val rod = this.internalFuelRod ?: return
        this.depletionPoint -= fuelDepletion * rod.weight
    }

    override fun getController(): FissionReactor? {
        val controllers = getControllers()
        if (controllers.isEmpty()) return null
        return controllers.firstOrNull() as? FissionReactor
    }

    fun getCurrentDepletionRatio(): Double {
        val partialFuel = this.partialFuel ?: return 0.0
        val controller = getController()
        val reactor = controller?.reactor
        if (controller == null || !controller.isLocked() || reactor == null) {
            return 1.0 - depletionPoint / partialFuel.duration.toDouble()
        }
        return 1.0 - (depletionPoint - reactor.fuelDepletion * internalFuelRod!!.weight) /
                partialFuel.duration.toDouble()
    }

    fun voidPartialFuel() {
        val controller = getController()
        if (controller != null && controller.isLocked()) return
        setPartialFuel(null)
        depletionPoint = 0.0
        // depletionPoint is now 0, so the property setter is no longer guarded out.
        locked = false
    }

    override fun createUI(entityPlayer: Player): ModularUI {
        return ModularUI(176, 166, this, entityPlayer)
            .background(GuiTextures.BACKGROUND)
            .widget(LabelWidget(10, 6, definition.descriptionId))
            .widget(
                SlotWidget(lockableInventory, 0, 40, 18, true, true)
                    .setBackground(GuiTextures.SLOT)
            )
            .widget(
                SlotWidget(partialFuelDisplay, 0, 118, 18, false, false)
                    .setBackground(GuiTextures.MAINTENANCE_ICON)
            )
            .widget(
                ButtonWidget(
                    140, 18, 18, 18, GuiTextures.BUTTON_VOID
                ) { cd: ClickData ->
                    if (!cd.isRemote) {
                        voidPartialFuel()
                    }
                }.setHoverTooltips(Component.translatable("supercritical.gui.void_fuel").string)
            )
            .widget(
                ComponentPanelWidget(10, 43) { text: MutableList<Component> -> addDepletionText(text) }
                    .textSupplier(
                        if (level!!.isClientSide) null
                        else { text: MutableList<Component> -> addDepletionText(text) }
                    ).setMaxWidthLimit(150)
            )
            .widget(UITemplate.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 84, true))
    }

    private fun addDepletionText(text: MutableList<Component>) {
        text.add(
            Component.translatable(
                "supercritical.gui.fission.depletion",
                String.format("%.2f", getCurrentDepletionRatio() * 100)
            )
        )
        val depleted = depletedFuel
        val rodName = depleted.displayName.string
        text.add(
            Component.translatable(
                "supercritical.gui.fission.depleted_rod",
                rodName.take(18) + "..."
            )
        )
    }

    override fun onLoad() {
        super.onLoad()
        subscribeServerTick {
            if (offsetTimer % 5 == 0L && isWorkingEnabled) {
                lockableInventory.importFromNearby(frontFacing)
            }
        }
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        tag.putBoolean("Locked", lockableInventory.locked)
        val partialFuelId = partialFuel?.id
        if (!partialFuelId.isNullOrBlank()) tag.putString("PartialFuel", partialFuelId)
        tag.putDouble("DepletionPoint", depletionPoint)
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        lockableInventory.locked = tag.getBoolean("Locked")
        if (tag.contains("PartialFuel")) {
            this.partialFuel = FissionFuelRegistry.getFissionFuel(tag.getString("PartialFuel"))
        }
        this.depletionPoint = tag.getDouble("DepletionPoint")
    }
}
