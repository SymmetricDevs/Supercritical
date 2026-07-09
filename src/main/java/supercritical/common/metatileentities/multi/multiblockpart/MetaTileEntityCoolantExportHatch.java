package supercritical.common.metatileentities.multi.multiblockpart;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;

import supercritical.api.capability.ICoolantHandler;
import supercritical.api.capability.impl.LockableFluidTank;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.nuclear.fission.ICoolantStats;

public class MetaTileEntityCoolantExportHatch extends TieredIOPartMachine
        implements ICoolantHandler, IControllable, IFissionReactorHatch {

    @Persisted
    @DescSynced
    @RequireRerender
    private boolean workingEnabled;
    private final LockableFluidTank fluidTank;
    private ICoolantStats coolant;

    public MetaTileEntityCoolantExportHatch(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.OUT);
        this.workingEnabled = true;
        this.fluidTank = new LockableFluidTank(this, 16000, IO.OUT);
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
        return true;
    }

    @Override
    public void setLock(boolean isLocked) {
        fluidTank.setLock(isLocked);
    }

    @Override
    public boolean isLocked() {
        return fluidTank.isLocked();
    }

    @Override
    public Fluid getLockedObject() {
        return fluidTank.getLockedObject();
    }

    @Override
    public @Nullable ICoolantStats getCoolant() {
        return this.coolant;
    }

    @Override
    public void setCoolant(@Nullable ICoolantStats prop) {
        this.coolant = prop;
    }

    @Override
    public @NotNull LockableFluidTank getFluidTank() {
        return this.fluidTank;
    }

    @Override
    public @Nullable ICoolantHandler getOutputHandler() {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(() -> {
            if (getOffsetTimer() % 5 == 0 && isWorkingEnabled()) {
                fluidTank.exportToNearby(getFrontFacing());
            }
        });
    }

    @Override
    public void saveCustomPersistedData(net.minecraft.nbt.CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putBoolean("Locked", fluidTank.isLocked());
    }

    @Override
    public void loadCustomPersistedData(net.minecraft.nbt.CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        fluidTank.setLock(tag.getBoolean("Locked"));
    }
}
