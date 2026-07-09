package supercritical.api.unification.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import supercritical.api.nuclear.fission.IFissionFuelStats;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FissionFuelProperty implements IMaterialProperty, IFissionFuelStats {

    private int maxTemperature;
    private int duration;
    private double slowNeutronCaptureCrossSection;
    private double fastNeutronCaptureCrossSection;
    private double slowNeutronFissionCrossSection;
    private double fastNeutronFissionCrossSection;
    private double neutronGenerationTime;
    private double releasedNeutrons;
    private double requiredNeutrons = 1;
    private double releasedHeatEnergy;
    private double decayRate;
    private ResourceLocation id;
    private Function<Double, ItemStack> depletedFuelSupplier = thermalRatio -> ItemStack.EMPTY;
    private Supplier<List<ItemStack>> allDepletedFuels = List::of;

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }

    public static Builder builder(ResourceLocation id, int maxTemperature, int duration, double neutronGenerationTime) {
        return new Builder().id(id).maxTemperature(maxTemperature).duration(duration).neutronGenerationTime(neutronGenerationTime);
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public int getDuration() {
        return duration;
    }

    public double getSlowNeutronCaptureCrossSection() {
        return slowNeutronCaptureCrossSection;
    }

    public double getFastNeutronCaptureCrossSection() {
        return fastNeutronCaptureCrossSection;
    }

    public double getSlowNeutronFissionCrossSection() {
        return slowNeutronFissionCrossSection;
    }

    public double getFastNeutronFissionCrossSection() {
        return fastNeutronFissionCrossSection;
    }

    public double getNeutronGenerationTime() {
        return neutronGenerationTime;
    }

    public double getReleasedNeutrons() {
        return releasedNeutrons;
    }

    public double getRequiredNeutrons() {
        return requiredNeutrons;
    }

    public double getReleasedHeatEnergy() {
        return releasedHeatEnergy;
    }

    public double getDecayRate() {
        return decayRate;
    }

    public String getId() {
        return id.toString();
    }

    public ResourceLocation getResourceLocation() {
        return id;
    }

    public List<ItemStack> getDepletedFuels() {
        return allDepletedFuels.get();
    }

    public ItemStack getDepletedFuel(double thermalRatio) {
        return depletedFuelSupplier.apply(thermalRatio);
    }

    public FissionFuelProperty setDepletedFuelSupplier(Function<Double, ItemStack> depletedFuelSupplier) {
        this.depletedFuelSupplier = depletedFuelSupplier;
        return this;
    }

    public FissionFuelProperty setAllDepletedFuels(Supplier<List<ItemStack>> allDepletedFuels) {
        this.allDepletedFuels = allDepletedFuels;
        return this;
    }

    public static final class Builder {
        private final FissionFuelProperty property = new FissionFuelProperty();

        public Builder id(ResourceLocation id) { property.id = id; return this; }
        public Builder maxTemperature(int maxTemperature) { property.maxTemperature = maxTemperature; return this; }
        public Builder duration(int duration) { property.duration = duration; return this; }
        public Builder slowNeutronCaptureCrossSection(double value) { property.slowNeutronCaptureCrossSection = value; return this; }
        public Builder fastNeutronCaptureCrossSection(double value) { property.fastNeutronCaptureCrossSection = value; return this; }
        public Builder slowNeutronFissionCrossSection(double value) { property.slowNeutronFissionCrossSection = value; return this; }
        public Builder fastNeutronFissionCrossSection(double value) { property.fastNeutronFissionCrossSection = value; return this; }
        public Builder neutronGenerationTime(double value) { property.neutronGenerationTime = value; return this; }
        public Builder releasedNeutrons(double value) { property.releasedNeutrons = value; return this; }
        public Builder requiredNeutrons(double value) { property.requiredNeutrons = value; return this; }
        public Builder releasedHeatEnergy(double value) { property.releasedHeatEnergy = value; return this; }
        public Builder decayRate(double value) { property.decayRate = value; return this; }
        public FissionFuelProperty build() { return property; }
    }
}
