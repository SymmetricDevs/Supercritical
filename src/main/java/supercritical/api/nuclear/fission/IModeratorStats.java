package supercritical.api.nuclear.fission;

public interface IModeratorStats {

    /**
     * @return The temperature that the moderator can handle before the reactor melts down.
     */
    int getMaxTemperature();

    /**
     * @return The moderation factor of the moderator. Theoretically, a value of x means that (1 - e^(-x)) fast neutrons
     *         are
     *         converted to slow neutrons in one meter.
     */
    double getModerationFactor();

    /**
     * @return The absorption factor of the moderator. Theoretically, a value of x means that (1 - e^(-x)) slow neutrons
     *         are
     *         absorbed in one meter.
     */
    double getAbsorptionFactor();
}
