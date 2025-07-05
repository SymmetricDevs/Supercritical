package supercritical.api.capability;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import supercritical.api.capability.impl.LockableFluidTank;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.nuclear.fission.ICoolantStats;

public interface ICoolantHandler extends ILockableHandler<Fluid>, IFissionReactorHatch {

    @Nullable
    ICoolantStats getCoolant();

    void setCoolant(@Nullable ICoolantStats prop);

    @NotNull
    LockableFluidTank getFluidTank();

    @NotNull
    EnumFacing getFrontFacing();

    ICoolantHandler getOutputHandler();
}
