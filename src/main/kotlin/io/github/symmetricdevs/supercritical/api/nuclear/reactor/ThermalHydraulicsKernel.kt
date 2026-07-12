package io.github.symmetricdevs.supercritical.api.nuclear.reactor

import net.minecraft.nbt.CompoundTag

interface ThermalHydraulicsKernel {
    fun precompute(geometry: ReactorGeometry)
    fun transfer(state: ReactorState, neutronics: NeutronicsResult, dt: Double): HeatTransferResult
    fun save(tag: CompoundTag)
    fun load(tag: CompoundTag)
}

data class HeatTransferResult(
    val heatRemoved: Double,
    val voidFraction: Double,
    val hydrogenProduced: Double,
    val coolantOutletTemperature: Double
)
