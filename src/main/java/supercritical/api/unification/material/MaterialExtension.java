package supercritical.api.unification.material;

import gregtech.api.unification.material.Material;
import lombok.experimental.UtilityClass;

public interface MaterialExtension {

    // Assumes one mole per item and that it's always "starting to decay"
    double getDecaysPerSecond();

    @UtilityClass
    class Handler {

        public double getDecaysPerSecond(Material material) {
            return ((MaterialExtension) material).getDecaysPerSecond();
        }
    }
}
