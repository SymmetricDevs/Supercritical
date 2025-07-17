package supercritical.api.unification.material.properties;

import net.minecraftforge.fluids.Fluid;

import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import lombok.Getter;
import lombok.Setter;
import supercritical.api.nuclear.fission.ICoolantStats;

public class CoolantProperty implements IMaterialProperty, ICoolantStats {

    @Setter
    @Getter
    private Material hotHPCoolant;
    @Setter
    @Getter
    private double moderatorFactor;
    /**
     * Roughly the heat transfer coefficient Do not put too much thought into this
     */
    @Setter
    @Getter
    private double coolingFactor;
    // in kelvin at standard conditions
    @Setter
    @Getter
    private double boilingPoint;
    // neutron absorption rate
    // in J/L
    @Setter
    @Getter
    private double heatOfVaporization;
    // in J/(kg*K)
    @Setter
    @Getter
    private double specificHeatCapacity;
    private boolean accumulatesHydrogen = false;

    @Setter
    @Getter
    private double slowAbsorptionFactor = 0;

    @Setter
    @Getter
    private double fastAbsorptionFactor = 0;
    // To store the specific key
    private final FluidStorageKey key;

    @Getter
    private final double mass;

    public CoolantProperty(Material mat, Material hotHPCoolant, FluidStorageKey key, double moderatorFactor,
                           double coolingFactor,
                           double boilingPoint, double heatOfVaporization,
                           double specificHeatCapacity) {
        this.hotHPCoolant = hotHPCoolant;
        this.moderatorFactor = moderatorFactor;
        this.coolingFactor = coolingFactor;
        this.boilingPoint = boilingPoint;
        this.heatOfVaporization = heatOfVaporization;
        this.specificHeatCapacity = specificHeatCapacity;
        this.key = key;
        this.mass = mat.getMass();
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.FLUID, true);
    }

    public boolean accumulatesHydrogen() {
        return accumulatesHydrogen;
    }

    public CoolantProperty setAccumulatesHydrogen(boolean accumulatesHydrogen) {
        this.accumulatesHydrogen = accumulatesHydrogen;
        return this;
    }

    public Fluid getHotCoolant() {
        return hotHPCoolant.getFluid();
    }

    public CoolantProperty setFastAbsorptionFactor(double fastAbsorptionFactor) {
        this.fastAbsorptionFactor = fastAbsorptionFactor;
        return this;
    }

    public CoolantProperty setSlowAbsorptionFactor(double slowAbsorptionFactor) {
        this.slowAbsorptionFactor = slowAbsorptionFactor;
        return this;
    }

    public FluidStorageKey getCoolantKey() {
        return key;
    }
}
