package supercritical.api.cover;

/**
 * Allows a MetaTileEntity to report custom EU capacity/stored values to energy detector covers.
 */
public interface ICustomEnergyCover {

    /**
     * @return the total EU capacity visible to detector covers
     */
    long getCoverCapacity();

    /**
     * @return the stored EU visible to detector covers
     */
    long getCoverStored();
}
