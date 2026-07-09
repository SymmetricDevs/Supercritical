package supercritical.api.unification.material;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

/**
 * Provides Supercritical's decay-per-second calculation on top of GTCEu materials.
 */
public interface MaterialExtension {

    /**
     * Returns the number of radioactive decays per second for one mole of this material,
     * assuming the material is "starting to decay".
     */
    double getDecaysPerSecond();

    /**
     * Helper for callers that prefer a static-style method.
     */
    static double getDecaysPerSecond(Material material) {
        return ((MaterialExtension) (Object) material).getDecaysPerSecond();
    }
}
