package supercritical.api.nuclear.fission;

import net.minecraftforge.fluids.Fluid;

public interface ICoolantStats {

    /**
     * @return The heated coolant fluid.
     */
    Fluid getHotCoolant();

    /**
     * @return The specific heat capacity of the fluid in J/(kg*K).
     */
    double getSpecificHeatCapacity();

    /**
     * @return A factor relating to the neutron moderation properties; the higher the factor, the more fast neutrons
     * are converted to slow neutrons.
     */
    double getModeratorFactor();

    /**
     * @return A factor relating to the neutron absorption properties; the higher the factor, the more slow neutrons are
     *         absorbed.
     */
    double getSlowAbsorptionFactor();

    /**
     * @return A factor relating to the neutron absorption properties; the higher the factor, the more fast neutrons are
     *         absorbed.
     */
    double getFastAbsorptionFactor();

    /**
     * @return A rough heat transfer coefficient for the fluid.
     */
    double getCoolingFactor();

    /**
     * @return The boiling point of the fluid in Kelvin.
     */
    double getBoilingPoint();

    /**
     * @return The heat of vaporization of the fluid in J/(kg*K).
     */
    double getHeatOfVaporization();

    /**
     * @return If the coolant reacts with zirconium cladding at high temperatures. This is true for mostly water-based
     *         coolants.
     */
    boolean accumulatesHydrogen();

    /**
     * @return The mass of the coolant per liter in kg
     */
    double getMass();
}
