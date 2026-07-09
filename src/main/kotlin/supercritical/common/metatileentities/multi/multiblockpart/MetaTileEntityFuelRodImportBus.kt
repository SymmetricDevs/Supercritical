package supercritical.common.metatileentities.multi.multiblockpart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;

import supercritical.api.capability.IFuelRodHandler;
import supercritical.api.items.itemhandlers.LockableItemStackHandler;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.nuclear.fission.FissionFuelRegistry;
import supercritical.api.nuclear.fission.IFissionFuelStats;
import supercritical.api.nuclear.fission.components.FuelRod;
import supercritical.common.registry.SCBlocks;
import supercritical.common.metatileentities.multi.MetaTileEntityFissionReactor;

public class MetaTileEntityFuelRodImportBus extends TieredIOPartMachine
        implements IFuelRodHandler, IControllable, IFissionReactorHatch {

    @Persisted
    @DescSynced
    @RequireRerender
    private boolean workingEnabled;
    @Nullable
    private MetaTileEntityFissionReactor controller;
    private IFissionFuelStats fuelProperty;
    private MetaTileEntityFuelRodExportBus pairedHatch;
    private IFissionFuelStats partialFuel;
    private FuelRod internalFuelRod;
    private double depletionPoint;

    private final LockableItemStackHandler lockableInventory;

    public MetaTileEntityFuelRodImportBus(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.IN);
        this.workingEnabled = true;
        this.lockableInventory = new LockableItemStackHandler(this, IO.IN);
    }

    public NotifiableItemStackHandler getInventory() {
        return lockableInventory;
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
    }

    @Override
    public boolean checkValidity(int depth) {
        this.pairedHatch = getExportHatch(depth);
        return pairedHatch != null;
    }

    public MetaTileEntityFuelRodExportBus getExportHatch(int depth) {
        BlockPos.MutableBlockPos pos = getPos().mutable();
        Direction back = getFrontFacing().getOpposite();
        for (int i = 1; i < depth; i++) {
            pos.move(back);
            if (getLevel().getBlockState(pos).getBlock() != SCBlocks.FUEL_CHANNEL.get()) {
                return null;
            }
        }
        pos.move(back);
        if (MetaMachine.getMachine(getLevel(), pos) instanceof MetaTileEntityFuelRodExportBus export) {
            return export;
        }
        return null;
    }

    @Override
    public void setLock(boolean isLocked) {
        if (depletionPoint == 0) {
            lockableInventory.setLock(isLocked);
        }
    }

    @Override
    public boolean isLocked() {
        return lockableInventory.isLocked();
    }

    @Override
    public ItemStack getLockedObject() {
        return lockableInventory.getLockedObject();
    }

    @Override
    public IFissionFuelStats getFuel() {
        return this.fuelProperty;
    }

    @Override
    public void setFuel(IFissionFuelStats prop) {
        this.fuelProperty = prop;
    }

    @Override
    public IFissionFuelStats getPartialFuel() {
        return this.partialFuel;
    }

    @Override
    public boolean setPartialFuel(IFissionFuelStats prop) {
        if (prop == this.partialFuel) return false;
        this.partialFuel = prop;
        if (prop == null) {
            this.internalFuelRod = null;
        } else if (this.internalFuelRod != null) {
            this.internalFuelRod.setFuel(prop);
        }
        return true;
    }

    @Override
    public void setInternalFuelRod(FuelRod rod) {
        this.internalFuelRod = rod;
    }

    @Override
    public boolean isDepleted(double totalDepletion) {
        return this.depletionPoint <= totalDepletion * this.internalFuelRod.getWeight();
    }

    @Override
    public void markUndepleted() {
        if (this.partialFuel != null) {
            this.depletionPoint += this.partialFuel.getDuration();
        }
    }

    @Override
    public LockableItemStackHandler getInputStackHandler() {
        return this.lockableInventory;
    }

    @Override
    public NotifiableItemStackHandler getOutputStackHandler(int depth) {
        MetaTileEntityFuelRodExportBus export = getExportHatch(depth);
        return export == null ? null : export.getInventory();
    }

    @Override
    public void resetDepletion(double fuelDepletion) {
        if (this.internalFuelRod == null) return;
        this.depletionPoint -= fuelDepletion * this.internalFuelRod.getWeight();
    }

    @Override
    public ItemStack getDepletedFuel() {
        if (this.internalFuelRod == null) return ItemStack.EMPTY;
        return this.internalFuelRod.getDepletedFuel();
    }

    @Override
    public double getDepletionPoint() {
        return this.depletionPoint;
    }

    public double getCurrentDepletionRatio() {
        if (this.partialFuel == null) return 0;
        MetaTileEntityFissionReactor controller = getController();
        if (controller == null || !controller.isLocked() || controller.getReactor() == null) {
            return 1 - (depletionPoint / partialFuel.getDuration());
        }
        return 1 - ((depletionPoint - (controller.getReactor().fuelDepletion * internalFuelRod.getWeight()))
                / partialFuel.getDuration());
    }

    public void voidPartialFuel() {
        MetaTileEntityFissionReactor controller = getController();
        if (controller != null && controller.isLocked()) return;
        setPartialFuel(null);
        depletionPoint = 0;
        setLock(false);
    }

    @Override
    public MetaTileEntityFissionReactor getController() {
        var controllers = getControllers();
        if (controllers.isEmpty()) return null;
        if (controllers.first() instanceof MetaTileEntityFissionReactor reactor) {
            return reactor;
        }
        return null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(() -> {
            if (getOffsetTimer() % 5 == 0 && isWorkingEnabled()) {
                lockableInventory.importFromNearby(getFrontFacing());
            }
        });
    }

    @Override
    public void saveCustomPersistedData(net.minecraft.nbt.CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putBoolean("Locked", lockableInventory.isLocked());
        if (partialFuel != null) tag.putString("PartialFuel", partialFuel.getId());
        tag.putDouble("DepletionPoint", depletionPoint);
    }

    @Override
    public void loadCustomPersistedData(net.minecraft.nbt.CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        lockableInventory.setLock(tag.getBoolean("Locked"));
        if (tag.contains("PartialFuel")) {
            this.partialFuel = FissionFuelRegistry.getFissionFuel(tag.getString("PartialFuel"));
        }
        this.depletionPoint = tag.getDouble("DepletionPoint");
    }
}
