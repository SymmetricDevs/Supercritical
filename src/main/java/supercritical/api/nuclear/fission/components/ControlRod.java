package supercritical.api.nuclear.fission.components;

import java.util.List;

public class ControlRod extends ReactorComponent {

    private double weight;
    private final boolean tipModeration;
    private int relatedFuelRodPairs;

    public ControlRod(double maxTemperature, boolean tipModeration, double thermalConductivity, double mass) {
        super(0, maxTemperature, thermalConductivity, mass, true);
        this.tipModeration = tipModeration;
    }

    public static void normalizeWeights(List<ControlRod> effectiveControlRods, double totalWeight, double totalWorth) {
        if (totalWeight == 0) return;
        for (ControlRod rod : effectiveControlRods) {
            rod.weight = rod.weight / totalWeight * totalWorth;
        }
    }

    public static double controlRodFactor(List<ControlRod> effectiveControlRods, double insertion) {
        double factor = 0;
        for (ControlRod rod : effectiveControlRods) {
            if (rod.hasModeratorTip()) {
                if (insertion <= 0.3) {
                    factor -= insertion / 3 * rod.weight;
                } else {
                    factor -= (-11D / 7 * (insertion - 0.3) + 0.1) * rod.weight;
                }
            } else {
                factor += insertion * rod.weight;
            }
        }
        return factor;
    }

    @Override
    public double getAbsorptionFactor(boolean controlsInserted, boolean thermal) {
        return controlsInserted ? 4 : 0;
    }

    public void addFuelRodPair() {
        relatedFuelRodPairs++;
    }

    public boolean hasModeratorTip() {
        return tipModeration;
    }

    public void computeWeightFromFuelRodMap() {
        weight = relatedFuelRodPairs * 4;
    }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
}
