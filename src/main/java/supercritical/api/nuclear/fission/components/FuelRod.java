package supercritical.api.nuclear.fission.components;

import net.minecraft.world.item.ItemStack;
import supercritical.api.nuclear.fission.IFissionFuelStats;

public class FuelRod extends ReactorComponent {

    private IFissionFuelStats fuel;
    private double weight = 1;
    private double thermalProportion;

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

    public void setFuel(IFissionFuelStats fuel) {
        this.fuel = fuel;
        this.maxTemperature = fuel.getMaxTemperature();
    }

    public ItemStack getDepletedFuel() {
        return fuel.getDepletedFuel(thermalProportion);
    }

    public IFissionFuelStats getFuel() { return fuel; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public double getThermalProportion() { return thermalProportion; }
    public void setThermalProportion(double thermalProportion) { this.thermalProportion = thermalProportion; }
}
