package supercritical.api.nuclear.fission.components;

public class ReactorComponent {

    private final double moderationFactor;
    protected double maxTemperature;
    private final double thermalConductivity;
    private final double mass;
    private int x;
    private int y;
    private final boolean valid;
    private int index = -1;

    public ReactorComponent(double moderationFactor, double maxTemperature, double thermalConductivity, double mass,
                            boolean valid) {
        this.moderationFactor = moderationFactor;
        this.maxTemperature = maxTemperature;
        this.thermalConductivity = thermalConductivity;
        this.mass = mass;
        this.valid = valid;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double getAbsorptionFactor(boolean controlsInserted, boolean thermal) {
        return 0;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean samePositionAs(ReactorComponent component) {
        return x == component.x && y == component.y;
    }

    public double getDistance(ReactorComponent component) {
        return Math.sqrt(Math.pow(x - component.x, 2) + Math.pow(y - component.y, 2));
    }

    public double getModerationFactor() { return moderationFactor; }
    public double getMaxTemperature() { return maxTemperature; }
    public double getThermalConductivity() { return thermalConductivity; }
    public double getMass() { return mass; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
}
