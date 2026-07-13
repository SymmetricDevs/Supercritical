package io.github.symmetricdevs.supercritical.api.nuclear.reactor

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Entity
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.World
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ICoolantStats
import io.github.symmetricdevs.supercritical.api.nuclear.fission.IFissionFuelStats
import io.github.symmetricdevs.supercritical.api.nuclear.fission.IModeratorStats
import net.minecraft.nbt.CompoundTag

/** The single handle the multiblock controller holds. */
interface ReactorCore {
    val family: ReactorFamily
    val world: World
    val state: ReactorState
    val limits: ReactorLimits
    val control: ControlMechanism

    /** Runtime switches exposed to the controller. */
    var isOn: Boolean
    var controlRodRegulationOn: Boolean
    var controlRodInsertion: Double

    /** Live physics mirrors used by the regulator and GUI. */
    val temperature: Double
    val pressure: Double

    /** Coolant temperatures used by the control-rod regulator. */
    val coolantBaseTemperature: Double
    val coolantExitTemperature: Double

    /** Previous-tick temperature used by the regulator. */
    val prevTemperature: Double

    fun precompute()
    fun tick()
    fun save(tag: CompoundTag): CompoundTag
    fun load(tag: CompoundTag)

    fun turnOff()

    /**
     * Places a control rod at a lattice cell, returning the cell's [Entity].
     * [hasModeratorTip] selects a moderating-tip rod.
     */
    fun setControlRod(
        x: Int, y: Int, hasModeratorTip: Boolean,
        maxTemperature: Double, thermalConductivity: Double, mass: Double
    ): Entity

    /**
     * Places a moderator block at a lattice cell from [moderator] stats, returning the cell's
     * [Entity]. The cell's max temperature is derived from [IModeratorStats.maxTemperature].
     */
    fun setModerator(
        x: Int, y: Int, moderator: IModeratorStats,
        thermalConductivity: Double, mass: Double
    ): Entity

    /**
     * Places a fuel-rod cell built directly from [fuel] + thermal params, returning the cell
     * [Entity]. The controller passes it to the fuel-rod hatch, which reads the rod's
     * eigenvalue-derived weight / thermal-proportion (via the ECS component) for depletion scaling.
     */
    fun setFuelRod(
        x: Int, y: Int, fuel: IFissionFuelStats,
        maxTemperature: Double, thermalConductivity: Double, mass: Double
    ): Entity

    /**
     * Places a coolant-channel cell built directly from [coolant] + thermal params, returning the
     * cell [Entity].
     */
    fun setCoolantChannel(
        x: Int, y: Int, coolant: ICoolantStats,
        maxTemperature: Double, thermalConductivity: Double, mass: Double
    ): Entity

    /** Removes all fuel depletion accumulated so far. */
    fun resetFuelDepletion()

    /** Resets thermal state to ambient conditions and shuts power off. */
    fun resetThermalState()

    /** Updates the control-rod bank to the requested insertion fraction. */
    fun updateControlRodInsertion(insertion: Double)

    /** Manual physics steps used by the controller in cooldown/unlocked mode. */
    fun updatePower()
    fun updateTemperature()
    fun updatePressure()
    fun updateNeutronPoisoning()
    fun regulateControlRods()

    // Display mirrors used by the controller.
    val kEff: Double
    val maxPower: Double
    val maxTemperature: Double
    val maxPressure: Double
    val fuelDepletion: Double
    val accumulatedHydrogen: Double
}
