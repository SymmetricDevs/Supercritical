package supercritical.api.unification.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import net.minecraft.world.level.material.Fluid;
import supercritical.api.nuclear.fission.ICoolantStats;

public final class CoolantProperty implements IMaterialProperty, ICoolantStats {

    private Material hotHPCoolant;
    private double moderatorFactor;
    private double coolingFactor;
    private double boilingPoint;
    private double heatOfVaporization;
    private double specificHeatCapacity;
    private boolean accumulatesHydrogen;
    private double slowAbsorptionFactor;
    private double fastAbsorptionFactor;
    private final FluidStorageKey key;
    private final double mass;

    public CoolantProperty(Material material, Material hotHPCoolant, FluidStorageKey key, double moderatorFactor,
                           double coolingFactor, double boilingPoint, double heatOfVaporization,
                           double specificHeatCapacity) {
        this.hotHPCoolant = hotHPCoolant;
        this.moderatorFactor = moderatorFactor;
        this.coolingFactor = coolingFactor;
        this.boilingPoint = boilingPoint;
        this.heatOfVaporization = heatOfVaporization;
        this.specificHeatCapacity = specificHeatCapacity;
        this.key = key;
        this.mass = material.getMass();
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.FLUID, true);
    }

    public Material getHotHPCoolant() {
        return hotHPCoolant;
    }

    public CoolantProperty setHotHPCoolant(Material hotHPCoolant) {
        this.hotHPCoolant = hotHPCoolant;
        return this;
    }

    public double getModeratorFactor() {
        return moderatorFactor;
    }

    public CoolantProperty setModeratorFactor(double moderatorFactor) {
        this.moderatorFactor = moderatorFactor;
        return this;
    }

    public double getCoolingFactor() {
        return coolingFactor;
    }

    public CoolantProperty setCoolingFactor(double coolingFactor) {
        this.coolingFactor = coolingFactor;
        return this;
    }

    public double getBoilingPoint() {
        return boilingPoint;
    }

    public CoolantProperty setBoilingPoint(double boilingPoint) {
        this.boilingPoint = boilingPoint;
        return this;
    }

    public double getHeatOfVaporization() {
        return heatOfVaporization;
    }

    public CoolantProperty setHeatOfVaporization(double heatOfVaporization) {
        this.heatOfVaporization = heatOfVaporization;
        return this;
    }

    public double getSpecificHeatCapacity() {
        return specificHeatCapacity;
    }

    public CoolantProperty setSpecificHeatCapacity(double specificHeatCapacity) {
        this.specificHeatCapacity = specificHeatCapacity;
        return this;
    }

    public boolean accumulatesHydrogen() {
        return accumulatesHydrogen;
    }

    public CoolantProperty setAccumulatesHydrogen(boolean accumulatesHydrogen) {
        this.accumulatesHydrogen = accumulatesHydrogen;
        return this;
    }

    public double getSlowAbsorptionFactor() {
        return slowAbsorptionFactor;
    }

    public CoolantProperty setSlowAbsorptionFactor(double slowAbsorptionFactor) {
        this.slowAbsorptionFactor = slowAbsorptionFactor;
        return this;
    }

    public double getFastAbsorptionFactor() {
        return fastAbsorptionFactor;
    }

    public CoolantProperty setFastAbsorptionFactor(double fastAbsorptionFactor) {
        this.fastAbsorptionFactor = fastAbsorptionFactor;
        return this;
    }

    public FluidStorageKey getCoolantKey() {
        return key;
    }

    public double getMass() {
        return mass;
    }

    public Fluid getHotCoolant() {
        return hotHPCoolant.getFluid();
    }
}
