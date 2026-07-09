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
    private var moderatorFactor: Double,
    private var coolingFactor: Double,
    private var boilingPoint: Double,
    private var heatOfVaporization: Double,
    private var specificHeatCapacity: Double
) : IMaterialProperty, ICoolantStats {
    private var accumulatesHydrogen = false
    private var slowAbsorptionFactor = 0.0
    private var fastAbsorptionFactor = 0.0
    private val mass: Double

    init {
        this.mass = material.getMass().toDouble()
    }

    override fun verifyProperty(properties: MaterialProperties) {
        properties.ensureSet<FluidProperty?>(PropertyKey.FLUID, true)
    }

    fun setHotHPCoolant(hotHPCoolant: Material): CoolantProperty {
        this.hotHPCoolant = hotHPCoolant
        return this
    }

    override fun getModeratorFactor(): Double {
        return moderatorFactor
    }

    fun setModeratorFactor(moderatorFactor: Double): CoolantProperty {
        this.moderatorFactor = moderatorFactor
        return this
    }

    override fun getCoolingFactor(): Double {
        return coolingFactor
    }

    fun setCoolingFactor(coolingFactor: Double): CoolantProperty {
        this.coolingFactor = coolingFactor
        return this
    }

    override fun getBoilingPoint(): Double {
        return boilingPoint
    }

    fun setBoilingPoint(boilingPoint: Double): CoolantProperty {
        this.boilingPoint = boilingPoint
        return this
    }

    override fun getHeatOfVaporization(): Double {
        return heatOfVaporization
    }

    fun setHeatOfVaporization(heatOfVaporization: Double): CoolantProperty {
        this.heatOfVaporization = heatOfVaporization
        return this
    }

    override fun getSpecificHeatCapacity(): Double {
        return specificHeatCapacity
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

    override fun getSlowAbsorptionFactor(): Double {
        return slowAbsorptionFactor
    }

    fun setSlowAbsorptionFactor(slowAbsorptionFactor: Double): CoolantProperty {
        this.slowAbsorptionFactor = slowAbsorptionFactor
        return this
    }

    override fun getFastAbsorptionFactor(): Double {
        return fastAbsorptionFactor
    }

    fun setFastAbsorptionFactor(fastAbsorptionFactor: Double): CoolantProperty {
        this.fastAbsorptionFactor = fastAbsorptionFactor
        return this
    }

    override fun getMass(): Double {
        return mass
    }

    override fun getHotCoolant(): Fluid? {
        return hotHPCoolant.getFluid()
    }
}
