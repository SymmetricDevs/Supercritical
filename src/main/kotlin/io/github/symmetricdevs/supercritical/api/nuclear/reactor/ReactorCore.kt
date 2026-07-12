package io.github.symmetricdevs.supercritical.api.nuclear.reactor

import net.minecraft.nbt.CompoundTag

/** The single handle the multiblock controller holds. */
interface ReactorCore {
    val family: ReactorFamily
    val geometry: ReactorGeometry
    val state: ReactorState
    val limits: ReactorLimits
    val control: ControlMechanism

    fun precompute()
    fun tick()
    fun save(tag: CompoundTag): CompoundTag
    fun load(tag: CompoundTag)
    fun turnOff()

    // Display mirrors used by the controller.
    val kEff: Double
    val maxPower: Double
    val maxTemperature: Double
    val maxPressure: Double
    val fuelDepletion: Double
    val accumulatedHydrogen: Double
}
