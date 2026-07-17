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
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import io.github.symmetricdevs.supercritical.api.machine.multiblock.FissionReactorHatch

class FuelRodExportBus(holder: IMachineBlockEntity, tier: Int) :
    TieredIOPartMachine(holder, tier, IO.OUT), IControllable, FissionReactorHatch, IUIMachine, IMachineLife {

    // capabilityIO = IO.BOTH so the controller can push depleted fuel rods into this output bus
    // via the public insertItem (NotifiableItemStackHandler.insertItem is guarded by canCapInput(),
    // which is false for IO.OUT — that guard made the controller's push fail and tripped
    // FUEL_CLOGGED the moment a rod depleted). handlerIO stays IO.OUT for recipe/handler-list
    // routing, and exportToNearby still works because canCapOutput() stays true. This mirrors the
    // capabilityIO=IO.BOTH trick LockableItemStackHandler uses for the import bus.
    val inventory: NotifiableItemStackHandler = NotifiableItemStackHandler(this, 1, IO.OUT, IO.BOTH)

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

    override fun onMachineRemoved() {
        // Pop depleted fuel rods sitting in the output slot loose as item entities on break.
        clearInventory(inventory.storage)
    }

    override fun createUI(entityPlayer: Player): ModularUI {
        return ModularUI(176, 166, this, entityPlayer)
            .background(GuiTextures.BACKGROUND)
            .widget(LabelWidget(10, 6, definition.descriptionId))
            .widget(
                // canTakeItems=true so the player can pull depleted rods out manually; canPutItems
                // =false because this is an output-only slot (items arrive via the controller push).
                SlotWidget(inventory, 0, 79, 18, true, false)
                    .setBackground(GuiTextures.SLOT)
            )
            .widget(UITemplate.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 84, true))
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        // inventory is a plain field (LDLib @Persisted auto-persistence doesn't reach Kotlin machine
        // traits here), so depleted fuel rods sitting in the output slot are never serialized by
        // default. Persist the storage manually so they survive a world save/reload.
        tag.put("Inventory", inventory.storage.serializeNBT())
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        if (tag.contains("Inventory")) {
            inventory.storage.deserializeNBT(tag.getCompound("Inventory"))
        }
    }
}
