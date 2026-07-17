package io.github.symmetricdevs.supercritical.api.fission.reactor.pwr

import io.github.symmetricdevs.supercritical.api.fission.ecs.Schedule
import io.github.symmetricdevs.supercritical.api.fission.ecs.SystemGroup
import io.github.symmetricdevs.supercritical.api.fission.reactor.systems.*

/**
 * Fixed per-tick execution schedule for the legacy PWR family.
 *
 * Only the per-tick systems live here. The precompute systems
 * (GeometryRebuildSystem / NeutronicsPrecomputeSystem / ThermalPrecomputeSystem) reset
 * coolantMass and maxTemperature and would clobber per-tick thermal state if they ran every
 * tick, so they are driven exclusively from
 * [io.github.symmetricdevs.supercritical.api.fission.reactor.ReactorCore.precompute].
 */
object PWRSchedule {

    fun create(): Schedule = Schedule().apply {
        add(SystemGroup.NEUTRONICS, NeutronicsSystem())
        add(SystemGroup.FUEL_CYCLE, FuelCycleSystem())
        add(SystemGroup.FUEL_HANDLING, FuelHandlingSystem())
        add(SystemGroup.THERMAL_HYDRAULICS, ThermalHydraulicsSystem())
        add(SystemGroup.CONTROL, NeutronPoisoningSystem())
        add(SystemGroup.CONTROL, ControlRodSystem())
    }
}
