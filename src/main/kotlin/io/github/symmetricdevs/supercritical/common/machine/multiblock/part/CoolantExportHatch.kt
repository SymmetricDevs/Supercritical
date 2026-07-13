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
import io.github.symmetricdevs.supercritical.api.capability.ICoolantHandler
import io.github.symmetricdevs.supercritical.api.machine.multiblock.IFissionReactorHatch
import io.github.symmetricdevs.supercritical.api.machine.trait.LockableFluidTank
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraftforge.fluids.FluidStack

class CoolantExportHatch(holder: IMachineBlockEntity, tier: Int) :
    TieredIOPartMachine(holder, tier, IO.OUT), ICoolantHandler, IControllable, IFissionReactorHatch, IUIMachine {

    // capabilityIO = IO.BOTH so the reactor sim (LegacyPWRThermalHydraulics.makeCoolantFlow) can push hot
    // coolant into this output tank via the public fill(). NotifiableFluidTank.fill is guarded
    // by canCapInput(), which is false for IO.OUT — a pure-OUT tank silently rejects the sim's
    // fill (returns 0), so no hot coolant is ever stored and exportToNearby has nothing to push.
    // handlerIO stays IO.OUT for recipe/handler-list routing, and exportToNearby still works
    // because canCapOutput() stays true. Mirrors the capabilityIO=IO.BOTH trick FuelRodExportBus
    // (and LockableItemStackHandler) use for output hatches that receive content from the controller.
    override val fluidTank: LockableFluidTank = LockableFluidTank(this, 16000, IO.OUT, IO.BOTH)

    override val coolantFrontFacing: Direction
        get() = frontFacing

    override val hatchPos: BlockPos
        get() = pos

    override var lockIntent: Boolean
        get() = fluidTank.lockIntent
        set(value) {
            fluidTank.lockIntent = value
        }

    override val stack: FluidStack
        get() = fluidTank.stack

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
        return if (fluidTank.lockIntent) {
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
        // fluidTank is a plain field (LDLib @Persisted auto-persistence doesn't reach Kotlin machine
        // traits here), so the fluid (type + amount) is never serialized by default. Persist it
        // manually so hot coolant in the slot survives a world save/reload.
        tag.put("Fluid", fluidTank.storages[0].serializeNBT())
        tag.putBoolean("Locked", fluidTank.lockIntent)
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        // Restore the fluid BEFORE flipping `locked`: the LockableFluidTank.locked setter
        // re-derives `lockedFluidStack` from getFluidInTank(0), so it must see the persisted fluid
        // or the lock sample ends up EMPTY and the lock filter is blanked.
        if (tag.contains("Fluid")) {
            fluidTank.storages[0].deserializeNBT(tag.getCompound("Fluid"))
        }
        fluidTank.lockIntent = tag.getBoolean("Locked")
    }
}
