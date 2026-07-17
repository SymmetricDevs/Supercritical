package io.github.symmetricdevs.supercritical.api.cover

/**
 * Allows a MetaTileEntity to report custom EU capacity/stored values to energy detector covers.
 */
interface CustomEnergyCover {
    /**
     * @return the total EU capacity visible to detector covers
     */
    val coverCapacity: Long

    /**
     * @return the stored EU visible to detector covers
     */
    val coverStored: Long
}
