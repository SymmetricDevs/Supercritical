package supercritical.api.unification.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import supercritical.api.nuclear.fission.IModeratorStats;

public final class ModeratorProperty implements IMaterialProperty, IModeratorStats {

    private final int maxTemperature;
    private final double moderationFactor;
    private final double absorptionFactor;

    private ModeratorProperty(Builder builder) {
        this.maxTemperature = builder.maxTemperature;
        this.moderationFactor = builder.moderationFactor;
        this.absorptionFactor = builder.absorptionFactor;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public double getModerationFactor() {
        return moderationFactor;
    }

    public double getAbsorptionFactor() {
        return absorptionFactor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int maxTemperature;
        private double moderationFactor;
        private double absorptionFactor;

        public Builder maxTemperature(int maxTemperature) { this.maxTemperature = maxTemperature; return this; }
        public Builder moderationFactor(double moderationFactor) { this.moderationFactor = moderationFactor; return this; }
        public Builder absorptionFactor(double absorptionFactor) { this.absorptionFactor = absorptionFactor; return this; }
        public ModeratorProperty build() { return new ModeratorProperty(this); }
    }
}
