package supercritical.api.nuclear.fission;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ModeratorStats implements IModeratorStats {
    @Getter
    private int maxTemperature;
    @Getter
    private double moderationFactor;
    @Getter
    private double absorptionFactor;
}
