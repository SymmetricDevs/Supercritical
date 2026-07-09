package supercritical.api.nuclear.fission;

public final class ModeratorStats implements IModeratorStats {

    private final int maxTemperature;
    private final double moderationFactor;
    private final double absorptionFactor;

    public ModeratorStats(int maxTemperature, double moderationFactor, double absorptionFactor) {
        this.maxTemperature = maxTemperature;
        this.moderationFactor = moderationFactor;
        this.absorptionFactor = absorptionFactor;
    }

    @Override
    public int getMaxTemperature() {
        return maxTemperature;
    }

    @Override
    public double getModerationFactor() {
        return moderationFactor;
    }

    @Override
    public double getAbsorptionFactor() {
        return absorptionFactor;
    }
}
