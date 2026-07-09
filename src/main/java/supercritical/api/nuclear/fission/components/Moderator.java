package supercritical.api.nuclear.fission.components;

import supercritical.api.nuclear.fission.IModeratorStats;

public class Moderator extends ReactorComponent {

    private final IModeratorStats moderator;

    public Moderator(double thermalConductivity, double mass, IModeratorStats moderator) {
        super(moderator.getModerationFactor(), moderator.getMaxTemperature(), thermalConductivity, mass, true);
        this.moderator = moderator;
    }

    @Override
    public double getAbsorptionFactor(boolean controlsInserted, boolean thermal) {
        return thermal ? moderator.getAbsorptionFactor() : 0;
    }

    public IModeratorStats getModerator() {
        return moderator;
    }
}
