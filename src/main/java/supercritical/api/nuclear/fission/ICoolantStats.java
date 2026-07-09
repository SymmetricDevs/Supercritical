package supercritical.api.nuclear.fission;

import net.minecraft.world.level.material.Fluid;

public interface ICoolantStats {

    Fluid getHotCoolant();

    double getSpecificHeatCapacity();

    double getModeratorFactor();

    double getSlowAbsorptionFactor();

    double getFastAbsorptionFactor();

    double getCoolingFactor();

    double getBoilingPoint();

    double getHeatOfVaporization();

    boolean accumulatesHydrogen();

    double getMass();
}
