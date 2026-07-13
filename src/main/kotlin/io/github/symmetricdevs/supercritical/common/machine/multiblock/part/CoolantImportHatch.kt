package io.github.symmetricdevs.supercritical.common.machine.multiblock.part

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.UITemplate
import com.gregtechceu.gtceu.api.gui.widget.TankWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.material.Fluid
import io.github.symmetricdevs.supercritical.api.capability.ICoolantHandler
import io.github.symmetricdevs.supercritical.api.machine.multiblock.IFissionReactorHatch
import io.github.symmetricdevs.supercritical.api.machine.trait.LockableFluidTank
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ICoolantStats
import io.github.symmetricdevs.supercritical.common.data.ScritBlocks

class CoolantImportHatch(holder: IMachineBlockEntity, tier: Int) :
    TieredIOPartMachine(holder, tier, IO.IN), ICoolantHandler, IControllable, IFissionReactorHatch, IUIMachine {
    override val fluidTank: LockableFluidTank
    override var coolant: ICoolantStats? = null
    private var pairedHatch: CoolantExportHatch? = null

    init {
        // capabilityIO = IO.BOTH so the reactor sim can drain cold coolant from this import tank
        // (NotifiableFluidTank.drain is guarded by canCapOutput(), which is false for IO.IN — that
        // guard made makeCoolantFlow's drain return EMPTY, skipping the channel and killing the
        // coolant loop). handlerIO stays IO.IN for routing; pipe-fill (canCapInput) still works.
        this.fluidTank = LockableFluidTank(this, 16000, IO.IN, IO.BOTH)
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

    override val outputHandler: ICoolantHandler?
        get() = pairedHatch


    override fun checkValidity(depth: Int): Boolean {
        this.pairedHatch = getExportHatch(depth)
        return pairedHatch != null
    }

    fun getExportHatch(depth: Int): CoolantExportHatch? {
        val level = level ?: return null
        val pos = (pos ?: return null).mutable()
        val back = frontFacing.opposite
        for (i in 1..<depth) {
            pos.move(back)
            if (level.getBlockState(pos).block !== ScritBlocks.COOLANT_CHANNEL.get()) {
                return null
            }
        }
        pos.move(back)
        return getMachine(level, pos) as? CoolantExportHatch
    }

    override fun createUI(entityPlayer: Player): ModularUI {
        val tankWidget = TankWidget(fluidTank, 0, 119, 52, true, false)
            .setShowAmount(false)
            .setBackground(GuiTextures.FLUID_SLOT)
        return ModularUI(176, 166, this, entityPlayer)
            .background(GuiTextures.BACKGROUND)
            .widget(LabelWidget(6, 6, definition.descriptionId))
            .widget(ImageWidget(7, 16, 131, 55, GuiTextures.DISPLAY))
            .widget(tankWidget)
            .widget(LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
            // White is the LabelWidget default (its constructor calls setTextColor(-1)), so the
            // deprecated setTextColor call is intentionally omitted here.
            .widget(LabelWidget(11, 30) { getFormattedFluidAmount() })
            .widget(LabelWidget(11, 40) { getFluidNameWithLock() })
            .widget(UITemplate.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 84, true))
    }

    private fun getFormattedFluidAmount(): String {
        return String.format("%,d", fluidTank.getFluidInTank(0).amount)
    }

    private fun getFluidNameWithLock(): String {
        val fluid = fluidTank.getFluidInTank(0)
        val name = if (fluid.isEmpty) "" else fluid.displayName.string
        return if (fluidTank.lockedState) {
            name + " " + Component.translatable("supercritical.gui.locked").string
        } else {
            name
        }
    }

    override fun onLoad() {
        super.onLoad()
        subscribeServerTick {
            if (offsetTimer % 5 == 0L && isWorkingEnabled) {
                fluidTank.importFromNearby(frontFacing)
            }
        }
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        // fluidTank is a plain field (LDLib @Persisted auto-persistence doesn't reach Kotlin machine
        // traits here), so the fluid (type + amount) is never serialized by default. Persist it
        // manually so coolant in the slot survives a world save/reload.
        tag.put("Fluid", fluidTank.storages[0].serializeNBT())
        tag.putBoolean("Locked", fluidTank.lockedState)
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        // Restore the fluid BEFORE flipping `lockedState`: the LockableFluidTank.lockedState setter
        // re-derives `lockedFluidStack` from getFluidInTank(0), so it must see the persisted fluid
        // or the lock sample ends up EMPTY and the lock filter is blanked.
        if (tag.contains("Fluid")) {
            fluidTank.storages[0].deserializeNBT(tag.getCompound("Fluid"))
        }
        fluidTank.lockedState = tag.getBoolean("Locked")
    }
}
