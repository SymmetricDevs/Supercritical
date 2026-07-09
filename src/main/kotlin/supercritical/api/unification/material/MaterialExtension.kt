package supercritical.api.unification.material

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
        fun getDecaysPerSecond(material: Material): Double {
            return (material as Any? as MaterialExtension).decaysPerSecond
        }
    }
}
