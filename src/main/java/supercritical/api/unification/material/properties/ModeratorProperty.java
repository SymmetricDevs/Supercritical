package supercritical.api.unification.material.properties;

import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import lombok.Builder;
import lombok.Getter;
import supercritical.api.nuclear.fission.IModeratorStats;

@Builder
public class ModeratorProperty implements IMaterialProperty, IModeratorStats {

    @Getter
    private int maxTemperature;
    @Getter
    private double moderationFactor;
    @Getter
    private double absorptionFactor;

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }
}
