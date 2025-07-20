package supercritical.api.unification.material.properties;

import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import it.unimi.dsi.fastutil.doubles.Double2ObjectFunction;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import supercritical.api.nuclear.fission.IFissionFuelStats;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Builder
public class FissionFuelProperty implements IMaterialProperty, IFissionFuelStats {

    // The max temperature the fuel can handle before it liquefies.
    @Getter
    private int maxTemperature;
    // Scales how long the fuel rod lasts in the reactor.
    @Getter
    private int duration;
    // How likely it is to absorb a neutron that had touched a moderator.
    @Getter
    private double slowNeutronCaptureCrossSection;
    // How likely it is to absorb a neutron that has not yet touched a moderator.
    @Getter
    private double fastNeutronCaptureCrossSection;
    // How likely it is for a moderated neutron to cause fission in this fuel.
    @Getter
    private double slowNeutronFissionCrossSection;
    // How likely it is for a not-yet-moderated neutron to cause fission in this fuel.
    @Getter
    private double fastNeutronFissionCrossSection;
    // The average time for a neutron to be emitted during a fission event. Do not make this accurate.
    @Getter
    private double neutronGenerationTime;
    @Getter
    private double releasedNeutrons;
    @Builder.Default()
    @Getter
    private double requiredNeutrons = 1;
    @Getter
    private double releasedHeatEnergy;
    @Getter
    private double decayRate;
    @Getter
    private String id;

    @Getter
    @Setter
    private Function<Double, ItemStack> depletedFuelSupplier;
    @Setter
    private Supplier<List<ItemStack>> allDepletedFuels;

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }

    @Override
    public List<ItemStack> getDepletedFuels() {
        return allDepletedFuels.get();
    }

    @Override
    public ItemStack getDepletedFuel(double thermalRatio) {
        return depletedFuelSupplier.apply(thermalRatio);
    }

    public static FissionFuelPropertyBuilder builder(String id, int maxTemperature, int duration,
                                                     double neutronGenerationTime) {
        return new FissionFuelPropertyBuilder()
                .id(id)
                .maxTemperature(maxTemperature)
                .duration(duration)
                .neutronGenerationTime(neutronGenerationTime);
    }
}
