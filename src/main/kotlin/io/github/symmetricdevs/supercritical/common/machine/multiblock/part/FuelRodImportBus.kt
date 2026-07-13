package io.github.symmetricdevs.supercritical.common.machine.multiblock.part

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.UITemplate
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
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
import io.github.symmetricdevs.supercritical.api.capability.IFuelRodHandler
import io.github.symmetricdevs.supercritical.api.machine.multiblock.IFissionReactorHatch
import io.github.symmetricdevs.supercritical.api.machine.trait.LockableItemStackHandler
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Entity
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.FuelRodComponent
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.ReactorComponentTypes
import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionFuelRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.fission.IFissionFuelStats
import io.github.symmetricdevs.supercritical.common.machine.multiblock.fission.FissionReactor
import io.github.symmetricdevs.supercritical.common.data.ScritBlocks

class FuelRodImportBus(holder: IMachineBlockEntity, tier: Int) : TieredIOPartMachine(holder, tier, IO.IN),
    IFuelRodHandler, IControllable, IFissionReactorHatch, IUIMachine, IMachineLife {
    override var fuel: IFissionFuelStats? = null
    override var partialFuel: IFissionFuelStats? = null
    private var fuelRodEntity: Entity? = null
    override var depletionPoint = 0.0

    private val lockableInventory: LockableItemStackHandler = LockableItemStackHandler(this, IO.IN)

    /**
     * Display-only item handler whose slot mirrors the currently locked fuel rod, matching the
     * legacy partial-fuel display slot.
     */
    private val partialFuelDisplay: ItemStackHandler = object : ItemStackHandler(1) {
        override fun getStackInSlot(slot: Int): ItemStack = this@FuelRodImportBus.lockedObject
    }

    val inventory: NotifiableItemStackHandler by ::lockableInventory

    override val inputStackHandler: LockableItemStackHandler
        get() = lockableInventory

    override val depletedFuel: ItemStack
        get() = fuelRodComponent()?.let { it.fuel.getDepletedFuel(it.thermalProportion) } ?: ItemStack.EMPTY

    override val hatchPos: BlockPos?
        get() = pos

    override var locked: Boolean
        get() = lockableInventory.locked
        set(value) {
            if (depletionPoint == 0.0) lockableInventory.locked = value
        }

    override val lockedObject: ItemStack by lockableInventory::lockedObject

    override fun checkValidity(depth: Int): Boolean = getExportHatch(depth) != null

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
            // Detach the cell entity so weight / depletedFuel reads go inert until the controller
            // re-binds a fresh entity on the next structure form (see addFuelRodComponent).
            this.fuelRodEntity = null
        }
        return true
    }

    override fun bindFuelRodEntity(entity: Entity?) {
        this.fuelRodEntity = entity
    }

    override fun isDepleted(totalDepletion: Double): Boolean {
        val weight = fuelRodComponent()?.weight ?: return false
        return this.depletionPoint <= totalDepletion * weight
    }

    override fun markUndepleted() {
        val partial = this.partialFuel ?: return
        this.depletionPoint += partial.duration.toDouble()
    }

    override fun getOutputStackHandler(depth: Int): NotifiableItemStackHandler? {
        return getExportHatch(depth)?.inventory
    }

    override fun resetDepletion(fuelDepletion: Double) {
        val weight = fuelRodComponent()?.weight ?: return
        this.depletionPoint -= fuelDepletion * weight
    }

    override fun getController(): FissionReactor? {
        val controllers = getControllers()
        if (controllers.isEmpty()) return null
        return controllers.firstOrNull() as? FissionReactor
    }

    /**
     * Live ECS fuel-rod component for the bound cell entity, or null when no rod is bound / the
     * controller isn't formed. The eigenvalue solver writes [FuelRodComponent.weight] and the
     * thermal precompute writes [FuelRodComponent.thermalProportion], so reads here stay live with
     * no copy step (component storage is by-reference — see [io.github.symmetricdevs.supercritical.api.nuclear.ecs.ComponentStorage]).
     */
    private fun fuelRodComponent(): FuelRodComponent? {
        val entity = fuelRodEntity ?: return null
        val world = getController()?.reactor?.world ?: return null
        return world.getComponent(entity, ReactorComponentTypes.FUEL_ROD)
    }

    fun getCurrentDepletionRatio(): Double {
        val partialFuel = this.partialFuel ?: return 0.0
        val controller = getController()
        val reactor = controller?.reactor
        val weight = fuelRodComponent()?.weight
        if (controller == null || !controller.isLocked() || reactor == null || weight == null) {
            return 1.0 - depletionPoint / partialFuel.duration.toDouble()
        }
        return 1.0 - (depletionPoint - reactor.fuelDepletion * weight) /
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

    override fun onMachineRemoved() {
        // Pop the real fuel-rod inventory loose as item entities on break. lockedObject is only a
        // count-1 filter SAMPLE (see LockableItemStackHandler), not a real rod, so it is intentionally
        // NOT cleared here — clearInventory is item-only and operates on the backing storage handler.
        clearInventory(lockableInventory.storage)
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        // lockableInventory is a plain field (not @Persisted on the machine, and no Kotlin
        // subclass declares its own MANAGED_FIELD_HOLDER), so LDLib never serializes the trait's
        // @Persisted storage/lockedObject. Persist them manually so fuel rods in the slot — and
        // the locked-rod sample used for the display slot + lock filter — survive reload.
        tag.put("Inventory", lockableInventory.storage.serializeNBT())
        tag.putBoolean("Locked", lockableInventory.locked)
        val lockedObj = lockableInventory.lockedObject
        if (!lockedObj.isEmpty) {
            tag.put("LockedObject", lockedObj.save(CompoundTag()))
        }
        val partialFuelId = partialFuel?.id
        if (!partialFuelId.isNullOrBlank()) tag.putString("PartialFuel", partialFuelId)
        tag.putDouble("DepletionPoint", depletionPoint)
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        // Restore the slot BEFORE flipping `locked`: the LockableItemStackHandler.locked setter
        // re-derives `lockedObject` from getStackInSlot(0), so it must see the persisted rods or
        // it blanks the sample. `lockedObject` is then re-applied below to cover the legitimate
        // "rod already extracted into partialFuel, slot empty" case where the sample rod must
        // still survive (otherwise insertItem rejects every stack because EMPTY != anything).
        if (tag.contains("Inventory")) {
            lockableInventory.storage.deserializeNBT(tag.getCompound("Inventory"))
        }
        lockableInventory.locked = tag.getBoolean("Locked")
        if (tag.contains("LockedObject")) {
            lockableInventory.lockedObject = ItemStack.of(tag.getCompound("LockedObject"))
        }
        if (tag.contains("PartialFuel")) {
            this.partialFuel = FissionFuelRegistry.getFissionFuel(tag.getString("PartialFuel"))
        }
        this.depletionPoint = tag.getDouble("DepletionPoint")
    }
}
