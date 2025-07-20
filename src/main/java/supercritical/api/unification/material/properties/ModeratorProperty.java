package supercritical.api.unification.material.properties;

import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fluids.Fluid;
import supercritical.api.nuclear.fission.ICoolantStats;
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
