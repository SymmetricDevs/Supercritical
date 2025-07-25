package supercritical.api.nuclear.fission.components;

import lombok.Getter;
import supercritical.api.nuclear.fission.IModeratorStats;

public class Moderator extends ReactorComponent {

    @Getter
    private IModeratorStats moderatorStats;

    public Moderator(IModeratorStats moderatorStats, double thermalConductivity, double mass) {
        super(moderatorStats.getModerationFactor(), moderatorStats.getMaxTemperature(), thermalConductivity, mass,
                true);
        this.moderatorStats = moderatorStats;
    }

    public double getAbsorptionFactor(boolean controlsInserted, boolean isThermal) {
        return moderatorStats.getAbsorptionFactor();
    }
}
