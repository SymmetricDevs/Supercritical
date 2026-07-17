package io.github.symmetricdevs.supercritical.api.fission.reactor

import io.github.symmetricdevs.supercritical.api.fission.ecs.registration.ComponentTypeRegistry
import io.github.symmetricdevs.supercritical.api.fission.ecs.registration.SystemRegistry

/**
 * Entrypoint for addon mods that want to extend the reactor simulation.
 *
 * Implementations are discovered via [java.util.ServiceLoader]; the service file
 * must be declared under `META-INF/services/`.
 */
interface ReactorAddonEntrypoint {

    /**
     * Registers custom component types. Called before families are instantiated.
     */
    fun registerComponents(registry: ComponentTypeRegistry) {}

    /**
     * Registers custom systems that families can reference when building schedules.
     */
    fun registerSystems(registry: SystemRegistry) {}

    /**
     * Registers custom reactor families.
     */
    fun registerReactorFamilies(registry: ReactorFamilyRegistry) {}
}
