package supercritical.api.unification.material.properties

import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import supercritical.api.nuclear.fission.IFissionFuelStats
import java.util.function.Function
import java.util.function.Supplier

class FissionFuelProperty : IMaterialProperty, IFissionFuelStats {
    private var maxTemperature = 0
    private var duration = 0
    private var slowNeutronCaptureCrossSection = 0.0
    private var fastNeutronCaptureCrossSection = 0.0
    private var slowNeutronFissionCrossSection = 0.0
    private var fastNeutronFissionCrossSection = 0.0
    private var neutronGenerationTime = 0.0
    private var releasedNeutrons = 0.0
    private var requiredNeutrons = 1.0
    private var releasedHeatEnergy = 0.0
    private var decayRate = 0.0
    private var id: ResourceLocation? = null
    private var depletedFuelSupplier = Function { thermalRatio: Double? -> ItemStack.EMPTY }
    private var allDepletedFuels: Supplier<MutableList<ItemStack?>?> = Supplier { mutableListOf() }

    override fun verifyProperty(properties: MaterialProperties) {
        properties.ensureSet<DustProperty?>(PropertyKey.DUST, true)
    }

    override fun getMaxTemperature(): Int {
        return maxTemperature
    }

    override fun getDuration(): Int {
        return duration
    }

    override fun getSlowNeutronCaptureCrossSection(): Double {
        return slowNeutronCaptureCrossSection
    }

    override fun getFastNeutronCaptureCrossSection(): Double {
        return fastNeutronCaptureCrossSection
    }

    override fun getSlowNeutronFissionCrossSection(): Double {
        return slowNeutronFissionCrossSection
    }

    override fun getFastNeutronFissionCrossSection(): Double {
        return fastNeutronFissionCrossSection
    }

    override fun getNeutronGenerationTime(): Double {
        return neutronGenerationTime
    }

    override fun getReleasedNeutrons(): Double {
        return releasedNeutrons
    }

    override fun getRequiredNeutrons(): Double {
        return requiredNeutrons
    }

    override fun getReleasedHeatEnergy(): Double {
        return releasedHeatEnergy
    }

    override fun getDecayRate(): Double {
        return decayRate
    }

    override fun getId(): String {
        return id.toString()
    }

    val resourceLocation: ResourceLocation
        get() = id

    override fun getDepletedFuels(): MutableList<ItemStack?>? {
        return allDepletedFuels.get()
    }

    override fun getDepletedFuel(thermalRatio: Double): ItemStack? {
        return depletedFuelSupplier.apply(thermalRatio)
    }

    fun setDepletedFuelSupplier(depletedFuelSupplier: Function<Double?, ItemStack?>): FissionFuelProperty {
        this.depletedFuelSupplier = depletedFuelSupplier
        return this
    }

    fun setAllDepletedFuels(allDepletedFuels: Supplier<MutableList<ItemStack?>?>): FissionFuelProperty {
        this.allDepletedFuels = allDepletedFuels
        return this
    }

    class Builder {
        private val property = FissionFuelProperty()

        fun id(id: ResourceLocation): Builder {
            property.id = id
            return this
        }

        fun maxTemperature(maxTemperature: Int): Builder {
            property.maxTemperature = maxTemperature
            return this
        }

        fun duration(duration: Int): Builder {
            property.duration = duration
            return this
        }

        fun slowNeutronCaptureCrossSection(value: Double): Builder {
            property.slowNeutronCaptureCrossSection = value
            return this
        }

        fun fastNeutronCaptureCrossSection(value: Double): Builder {
            property.fastNeutronCaptureCrossSection = value
            return this
        }

        fun slowNeutronFissionCrossSection(value: Double): Builder {
            property.slowNeutronFissionCrossSection = value
            return this
        }

        fun fastNeutronFissionCrossSection(value: Double): Builder {
            property.fastNeutronFissionCrossSection = value
            return this
        }

        fun neutronGenerationTime(value: Double): Builder {
            property.neutronGenerationTime = value
            return this
        }

        fun releasedNeutrons(value: Double): Builder {
            property.releasedNeutrons = value
            return this
        }

        fun requiredNeutrons(value: Double): Builder {
            property.requiredNeutrons = value
            return this
        }

        fun releasedHeatEnergy(value: Double): Builder {
            property.releasedHeatEnergy = value
            return this
        }

        fun decayRate(value: Double): Builder {
            property.decayRate = value
            return this
        }

        fun build(): FissionFuelProperty {
            return property
        }
    }

    companion object {
        fun builder(id: ResourceLocation, maxTemperature: Int, duration: Int, neutronGenerationTime: Double): Builder {
            return Builder().id(id).maxTemperature(maxTemperature).duration(duration)
                .neutronGenerationTime(neutronGenerationTime)
        }
    }
}
