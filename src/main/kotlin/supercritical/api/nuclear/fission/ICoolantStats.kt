package supercritical.api.nuclear.fission

interface ICoolantStats {
    val hotCoolant: Fluid?

    val specificHeatCapacity: Double

    val moderatorFactor: Double

    val slowAbsorptionFactor: Double

    val fastAbsorptionFactor: Double

    val coolingFactor: Double

    val boilingPoint: Double

    val heatOfVaporization: Double

    fun accumulatesHydrogen(): Boolean

    val mass: Double
}
