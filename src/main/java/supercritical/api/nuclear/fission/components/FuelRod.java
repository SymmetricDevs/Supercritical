package supercritical.api.nuclear.fission.components;

import lombok.Getter;
import supercritical.api.nuclear.fission.IFissionFuelStats;

public class FuelRod extends ReactorComponent {
    @Getter
    private IFissionFuelStats fuel;

    public FuelRod(double maxTemperature, double thermalConductivity, IFissionFuelStats fuel, double mass) {
        super(0, maxTemperature, thermalConductivity, mass, true);
        this.fuel = fuel;
    }

    public double getDuration() {
        return fuel.getDuration();
    }

    public double getNeutronGenerationTime() {
        return fuel.getNeutronGenerationTime();
    }


    public void setFuel(IFissionFuelStats property) {
        this.fuel = property;
        this.maxTemperature = property.getMaxTemperature();
    }
}
