package supercritical.api.nuclear.fission.components;

import supercritical.api.nuclear.fission.IFissionFuelStats;

public class FuelRod extends ReactorComponent {

    private IFissionFuelStats fuel;

    public FuelRod(double maxTemperature, double thermalConductivity, IFissionFuelStats fuel, double mass) {
        super(0, maxTemperature, thermalConductivity, mass, true);
        this.fuel = fuel;
    }

    public double getDuration() {
        return fuel.getDuration();
    }

    public double getHEFissionFactor() {
        return fuel.getFastNeutronFissionCrossSection();
    }

    public double getLEFissionFactor() {
        return fuel.getSlowNeutronFissionCrossSection();
    }


    public double getNeutronGenerationTime() {
        return fuel.getNeutronGenerationTime();
    }

    public IFissionFuelStats getFuel() {
        return fuel;
    }

    @Override
    public double getAbsorptionFactor(boolean controlsInserted, boolean isThermal) {
        if (isThermal) {
            return this.fuel.getSlowMicroscopicCaptureCrossSection();
        } else {
            return this.fuel.getFastMicroscopicCaptureCrossSection();
        }
    }

    public void setFuel(IFissionFuelStats property) {
        this.fuel = property;
        this.maxTemperature = property.getMaxTemperature();
    }
}
