package supercritical.common.machine.multiblock.part

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
import supercritical.api.capability.ICoolantHandler
import supercritical.api.machine.multiblock.IFissionReactorHatch
import supercritical.api.machine.trait.LockableFluidTank
import supercritical.api.nuclear.fission.ICoolantStats

class CoolantExportHatch(holder: IMachineBlockEntity, tier: Int) :
    TieredIOPartMachine(holder, tier, IO.OUT), ICoolantHandler, IControllable, IFissionReactorHatch, IUIMachine {
    override val fluidTank: LockableFluidTank
    override var coolant: ICoolantStats? = null

    init {
        // capabilityIO = IO.BOTH so the reactor sim (FissionReactor.makeCoolantFlow) can push hot
        // coolant into this output tank via the public fill(). NotifiableFluidTank.fill is guarded
        // by canCapInput(), which is false for IO.OUT — a pure-OUT tank silently rejects the sim's
        // fill (returns 0), so no hot coolant is ever stored and exportToNearby has nothing to push.
        // handlerIO stays IO.OUT for recipe/handler-list routing, and exportToNearby still works
        // because canCapOutput() stays true. Mirrors the capabilityIO=IO.BOTH trick FuelRodExportBus
        // (and LockableItemStackHandler) use for output hatches that receive content from the controller.
        this.fluidTank = LockableFluidTank(this, 16000, IO.OUT, IO.BOTH)
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
