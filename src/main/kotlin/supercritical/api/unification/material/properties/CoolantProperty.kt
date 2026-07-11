package supercritical.api.unification.material.properties

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey
import net.minecraft.world.level.material.Fluid
import supercritical.api.nuclear.fission.ICoolantStats

class CoolantProperty(
    material: Material,
    var hotHPCoolant: Material,
    val coolantKey: FluidStorageKey?,
    override var moderatorFactor: Double,
    override var coolingFactor: Double,
    override var boilingPoint: Double,
    override var heatOfVaporization: Double,
    override var specificHeatCapacity: Double
) : IMaterialProperty, ICoolantStats {
    private var accumulatesHydrogen = false
    override var slowAbsorptionFactor = 0.0
    override var fastAbsorptionFactor = 0.0
    override val mass: Double
    override val hotCoolant: Fluid?
        get() = hotHPCoolant.fluid

    init {
        mass = material.mass.toDouble()
    }

    override fun verifyProperty(properties: MaterialProperties) {
        properties.ensureSet<FluidProperty?>(PropertyKey.FLUID, true)
    }

    fun setHotHPCoolant(hotHPCoolant: Material): CoolantProperty {
        this.hotHPCoolant = hotHPCoolant
        return this
    }

    fun setModeratorFactor(moderatorFactor: Double): CoolantProperty {
        this.moderatorFactor = moderatorFactor
        return this
    }

    fun setCoolingFactor(coolingFactor: Double): CoolantProperty {
        this.coolingFactor = coolingFactor
        return this
    }

    fun setBoilingPoint(boilingPoint: Double): CoolantProperty {
        this.boilingPoint = boilingPoint
        return this
    }

    fun setHeatOfVaporization(heatOfVaporization: Double): CoolantProperty {
        this.heatOfVaporization = heatOfVaporization
        return this
    }

    fun setSpecificHeatCapacity(specificHeatCapacity: Double): CoolantProperty {
        this.specificHeatCapacity = specificHeatCapacity
        return this
    }

    override fun accumulatesHydrogen(): Boolean {
        return accumulatesHydrogen
    }

    fun setAccumulatesHydrogen(accumulatesHydrogen: Boolean): CoolantProperty {
        this.accumulatesHydrogen = accumulatesHydrogen
        return this
    }

    fun setSlowAbsorptionFactor(slowAbsorptionFactor: Double): CoolantProperty {
        this.slowAbsorptionFactor = slowAbsorptionFactor
        return this
    }

    fun setFastAbsorptionFactor(fastAbsorptionFactor: Double): CoolantProperty {
        this.fastAbsorptionFactor = fastAbsorptionFactor
        return this
    }

}
