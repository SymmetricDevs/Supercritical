package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.ecs.ComponentType
import io.github.symmetricdevs.supercritical.api.fission.ecs.registration.ComponentTypeRegistry
import io.github.symmetricdevs.supercritical.util.scId

/**
 * Well-known component types for the reactor ECS.
 *
 * These are registered by the PWR family and used by core systems. Addons may
 * register additional component types through [ComponentTypeRegistry].
 */
object ReactorComponentTypes {
    val REACTOR_STATE = ComponentType(
        id = scId("reactor_state"),
        clazz = ReactorStateComponent::class
    )
    val REACTOR_LIMITS = ComponentType(
        id = scId("reactor_limits"),
        clazz = ReactorLimitsComponent::class
    )
    val CONTROL_ROD_STATE = ComponentType(
        id = scId("control_rod_state"),
        clazz = ControlRodStateComponent::class
    )
    val NEUTRONICS_GLOBALS = ComponentType(
        id = scId("neutronics_globals"),
        clazz = NeutronicsGlobalsComponent::class
    )
    val THERMAL_GLOBALS = ComponentType(
        id = scId("thermal_globals"),
        clazz = ThermalGlobalsComponent::class
    )
    val LATTICE_GEOMETRY = ComponentType(
        id = scId("lattice_geometry"),
        clazz = LatticeGeometryComponent::class
    )
    val REACTOR_FAMILY = ComponentType(
        id = scId("reactor_family"),
        clazz = ReactorFamilyComponent::class
    )
    val POSITION = ComponentType(
        id = scId("position"),
        clazz = PositionComponent::class
    )
    val THERMAL_PROPERTIES = ComponentType(
        id = scId("thermal_properties"),
        clazz = ThermalPropertiesComponent::class
    )
    val NEUTRONICS_PROPERTIES = ComponentType(
        id = scId("neutronics_properties"),
        clazz = NeutronicsPropertiesComponent::class
    )
    val FUEL_ROD = ComponentType(
        id = scId("fuel_rod"),
        clazz = FuelRodComponent::class
    )
    val CONTROL_ROD = ComponentType(
        id = scId("control_rod"),
        clazz = ControlRodComponent::class
    )
    val COOLANT_CHANNEL = ComponentType(
        id = scId("coolant_channel"),
        clazz = CoolantChannelComponent::class
    )
    val MODERATOR = ComponentType(
        id = scId("moderator"),
        clazz = ModeratorComponent::class
    )

    fun registerAll(registry: ComponentTypeRegistry) {
        registry.register(REACTOR_STATE)
        registry.register(REACTOR_LIMITS)
        registry.register(CONTROL_ROD_STATE)
        registry.register(NEUTRONICS_GLOBALS)
        registry.register(THERMAL_GLOBALS)
        registry.register(LATTICE_GEOMETRY)
        registry.register(REACTOR_FAMILY)
        registry.register(POSITION)
        registry.register(THERMAL_PROPERTIES)
        registry.register(NEUTRONICS_PROPERTIES)
        registry.register(FUEL_ROD)
        registry.register(CONTROL_ROD)
        registry.register(COOLANT_CHANNEL)
        registry.register(MODERATOR)
    }
}
