package io.github.symmetricdevs.supercritical.api.data.chemical.material.property

import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import io.github.symmetricdevs.supercritical.api.fission.stats.FissionFuelStats
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import java.util.function.Function
import java.util.function.Supplier

class FissionFuelProperty(
    val resourceLocation: ResourceLocation,
    override var maxTemperature: Double,
    override var duration: Int,
    override var neutronGenerationTime: Double,
    override var slowNeutronCaptureCrossSection: Double = 0.0,
    override var fastNeutronCaptureCrossSection: Double = 0.0,
    override var slowNeutronFissionCrossSection: Double = 0.0,
    override var fastNeutronFissionCrossSection: Double = 0.0,
    override var releasedNeutrons: Double = 0.0,
    override var requiredNeutrons: Double = 1.0,
    override var releasedHeatEnergy: Double = 0.0,
    override var decayRate: Double = 0.0,
) : IMaterialProperty, FissionFuelStats {
    private var depletedFuelSupplier = Function { _: Double -> ItemStack.EMPTY }
    private var allDepletedFuels = Supplier { mutableListOf<ItemStack>() }

    override fun verifyProperty(properties: MaterialProperties) {
        properties.ensureSet(PropertyKey.DUST, true)
    }

    override val id: String
        get() = resourceLocation.toString()

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
}
