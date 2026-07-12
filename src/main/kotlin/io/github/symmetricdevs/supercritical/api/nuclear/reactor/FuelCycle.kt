package io.github.symmetricdevs.supercritical.api.nuclear.reactor

import net.minecraft.nbt.CompoundTag

interface FuelCycle {
    fun step(state: ReactorState, flux: Double, dt: Double)
    fun save(tag: CompoundTag)
    fun load(tag: CompoundTag)
}
