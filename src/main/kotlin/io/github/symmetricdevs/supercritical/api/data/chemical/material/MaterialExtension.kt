package io.github.symmetricdevs.supercritical.api.data.chemical.material

import com.gregtechceu.gtceu.api.data.chemical.material.Material

/**
 * Provides Supercritical's decay-per-second calculation on top of GTCEu materials.
 */
interface MaterialExtension {
    /**
     * Returns the number of radioactive decays per second for one mole of this material,
     * assuming the material is "starting to decay".
     */
    val decaysPerSecond: Double

    companion object {
        /**
         * Helper for callers that prefer a static-style method.
         */
        val Material.decaysPerSecond: Double
            get() = (this as MaterialExtension).decaysPerSecond
    }
}
