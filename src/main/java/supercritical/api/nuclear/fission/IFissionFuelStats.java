package supercritical.api.nuclear.fission;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IFissionFuelStats {

    int getMaxTemperature();

    int getDuration();

    double getSlowNeutronCaptureCrossSection();

    double getFastNeutronCaptureCrossSection();

    double getSlowNeutronFissionCrossSection();

    double getFastNeutronFissionCrossSection();

    double getReleasedNeutrons();

    double getRequiredNeutrons();

    double getReleasedHeatEnergy();

    double getDecayRate();

    double getNeutronGenerationTime();

    default int getNeutronGenerationTimeCategory() {
        if (getNeutronGenerationTime() > 2) {
            return 0;
        } else if (getNeutronGenerationTime() > 1.25) {
            return 1;
        } else if (getNeutronGenerationTime() > 0.9) {
            return 2;
        }
        return 3;
    }

    default double getFastFissionMultiplier() {
        return getFastNeutronFissionCrossSection() * getReleasedNeutrons() / getRequiredNeutrons();
    }

    default double getSlowFissionMultiplier() {
        return getSlowNeutronFissionCrossSection() * getReleasedNeutrons() / getRequiredNeutrons();
    }

    String getId();

    List<ItemStack> getDepletedFuels();

    ItemStack getDepletedFuel(double thermalRatio);
}
