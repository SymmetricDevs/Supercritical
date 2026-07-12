package io.github.symmetricdevs.supercritical.api.nuclear.reactor

import net.minecraft.nbt.CompoundTag

interface NeutronicsKernel {
    fun precompute(geometry: ReactorGeometry)
    fun solve(state: ReactorState, dt: Double): NeutronicsResult
    fun save(tag: CompoundTag)
    fun load(tag: CompoundTag)
}

data class NeutronicsResult(
    val kEff: Double,
    val kInf: Double,
    val totalFlux: Double,
    val controlWorth: Double,
    val thermalFraction: Double
)
