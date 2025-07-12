package supercritical.api.nuclear.fission.components;

import lombok.Getter;
import lombok.Setter;

public class ReactorComponent {

    @Getter
    private final double moderationFactor;
    @Getter
    protected double maxTemperature;
    @Getter
    private final double thermalConductivity;
    @Getter
    private final double mass;

    @Getter
    private int x;
    @Getter
    private int y;

    private final boolean isValid;

    // The index of the reactor component, which is -1 if unset
    @Setter
    @Getter
    private int index = -1;

    public ReactorComponent(double moderationFactor, double maxTemperature, double thermalConductivity, double mass,
                            boolean isValid) {
        this.moderationFactor = moderationFactor;
        this.maxTemperature = maxTemperature;
        this.thermalConductivity = thermalConductivity;
        this.mass = mass;
        this.isValid = isValid;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double getAbsorptionFactor(boolean controlsInserted, boolean isThermal) {
        return 0;
    }

    public boolean isValid() {
        return isValid;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean samePositionAs(ReactorComponent component) {
        return this.getX() == component.getX() && this.getY() == component.getY();
    }

    public double getDistance(ReactorComponent component) {
        return Math.sqrt(Math.pow(this.getX() - component.getX(), 2) +
                Math.pow(this.getY() - component.getY(), 2));
    }
}
