package supercritical.api.nuclear.fission

import net.minecraft.world.item.ItemStack

interface IFissionFuelStats {
    val maxTemperature: Int

    val duration: Int

    val slowNeutronCaptureCrossSection: Double

    val fastNeutronCaptureCrossSection: Double

    val slowNeutronFissionCrossSection: Double

    val fastNeutronFissionCrossSection: Double

    val releasedNeutrons: Double

    val requiredNeutrons: Double

    val releasedHeatEnergy: Double

    val decayRate: Double

    val neutronGenerationTime: Double

    val neutronGenerationTimeCategory: Int
        get() {
            if (this.neutronGenerationTime > 2) {
                return 0
            } else if (this.neutronGenerationTime > 1.25) {
                return 1
            } else if (this.neutronGenerationTime > 0.9) {
                return 2
            }
            return 3
        }

    val fastFissionMultiplier: Double
        get() = this.fastNeutronFissionCrossSection * this.releasedNeutrons / this.requiredNeutrons

    val slowFissionMultiplier: Double
        get() = this.slowNeutronFissionCrossSection * this.releasedNeutrons / this.requiredNeutrons

    val id: String?

    val depletedFuels: MutableList<ItemStack?>?

    fun getDepletedFuel(thermalRatio: Double): ItemStack?
}
