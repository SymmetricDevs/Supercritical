package supercritical.api.nuclear.fission.components;

import supercritical.api.capability.ICoolantHandler;
import supercritical.api.nuclear.fission.ICoolantStats;

public class CoolantChannel extends ReactorComponent {

    private final ICoolantStats coolant;
    private double weight;
    public double partialCoolant;
    private ICoolantHandler inputHandler;
    private ICoolantHandler outputHandler;

    public CoolantChannel(double maxTemperature, double thermalConductivity, ICoolantStats coolant, double mass) {
        super(coolant.getModeratorFactor(), maxTemperature, thermalConductivity, mass, true);
        this.coolant = coolant;
    }

    public void setHandlers(ICoolantHandler input, ICoolantHandler output) {
        this.inputHandler = input;
        this.outputHandler = output;
    }

    public ICoolantHandler getInputHandler() { return inputHandler; }
    public ICoolantHandler getOutputHandler() { return outputHandler; }

    public void addWeight(double weight) {
        this.weight += weight;
    }

    @Override
    public double getAbsorptionFactor(boolean controlsInserted, boolean thermal) {
        return thermal ? coolant.getSlowAbsorptionFactor() : coolant.getFastAbsorptionFactor();
    }

    public ICoolantStats getCoolant() { return coolant; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
}
