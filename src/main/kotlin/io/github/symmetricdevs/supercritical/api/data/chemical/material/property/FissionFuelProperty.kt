package io.github.symmetricdevs.supercritical.api.data.chemical.material.property

import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import io.github.symmetricdevs.supercritical.api.nuclear.fission.IFissionFuelStats
import java.util.function.Function
import java.util.function.Supplier

class FissionFuelProperty : IMaterialProperty, IFissionFuelStats {
    override var maxTemperature = 0
    override var duration = 0
    override var slowNeutronCaptureCrossSection = 0.0
    override var fastNeutronCaptureCrossSection = 0.0
    override var slowNeutronFissionCrossSection = 0.0
    override var fastNeutronFissionCrossSection = 0.0
    override var neutronGenerationTime = 0.0
    override var releasedNeutrons = 0.0
    override var requiredNeutrons = 1.0
    override var releasedHeatEnergy = 0.0
    override var decayRate = 0.0
    private var resourceId: ResourceLocation? = null
    private var depletedFuelSupplier = Function { _: Double -> ItemStack.EMPTY }
    private var allDepletedFuels: Supplier<MutableList<ItemStack>> = Supplier { mutableListOf() }

    override fun verifyProperty(properties: MaterialProperties) {
        properties.ensureSet(PropertyKey.DUST, true)
    }

    override val id: String?
        get() = resourceId?.toString()

    val resourceLocation: ResourceLocation
        get() = checkNotNull(resourceId) { "Fission fuel id has not been initialized" }

    override val depletedFuels: MutableList<ItemStack>
        get() = allDepletedFuels.get()

    override fun getDepletedFuel(thermalRatio: Double): ItemStack {
        return depletedFuelSupplier.apply(thermalRatio)
    }

    fun setDepletedFuelSupplier(depletedFuelSupplier: Function<Double, ItemStack>): FissionFuelProperty {
        this.depletedFuelSupplier = depletedFuelSupplier
        return this
    }

    fun setAllDepletedFuels(allDepletedFuels: Supplier<MutableList<ItemStack>>): FissionFuelProperty {
        this.allDepletedFuels = allDepletedFuels
        return this
    }

    class Builder {
        private val property = FissionFuelProperty()

        fun id(id: ResourceLocation): Builder {
            property.resourceId = id
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
