package supercritical.api.nuclear.fission.components

import net.minecraft.world.item.ItemStack
import supercritical.api.nuclear.fission.IFissionFuelStats

class FuelRod(maxTemperature: Double, thermalConductivity: Double, private var fuel: IFissionFuelStats, mass: Double) :
    ReactorComponent(0.0, maxTemperature, thermalConductivity, mass, true) {
    var weight: Double = 1.0
    var thermalProportion: Double = 0.0

    val duration: Double
        get() = fuel.duration.toDouble()

    val neutronGenerationTime: Double
        get() = fuel.neutronGenerationTime

    fun setFuel(fuel: IFissionFuelStats) {
        this.fuel = fuel
        this.maxTemperature = fuel.maxTemperature.toDouble()
    }

    val depletedFuel: ItemStack?
        get() = fuel.getDepletedFuel(thermalProportion)

    fun getFuel(): IFissionFuelStats {
        return fuel
    }
}
