package supercritical.api.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import supercritical.api.capability.impl.LockableFluidTank;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.nuclear.fission.ICoolantStats;

public interface ICoolantHandler extends ILockableHandler<Fluid>, IFissionReactorHatch {

    @Override
    boolean isLocked();

    @Override
    void setLock(boolean isLocked);

    @Nullable
    ICoolantStats getCoolant();

    void setCoolant(@Nullable ICoolantStats prop);

    @NotNull
    LockableFluidTank getFluidTank();

    @NotNull
    Direction getFrontFacing();

    @Nullable
    ICoolantHandler getOutputHandler();
}
