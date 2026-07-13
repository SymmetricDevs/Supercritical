package io.github.symmetricdevs.supercritical.api.nuclear.reactor.families

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Schedule
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.registration.ComponentTypeRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorFamily
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.pwr.PWRReactorSchedule
import net.minecraft.resources.ResourceLocation

object LegacyPWRFamily : ReactorFamily {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("supercritical", "pwr")

    override fun registerComponents(registry: ComponentTypeRegistry) {
        io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.ReactorComponentTypes.registerAll(registry)
    }

    override fun buildSchedule(): Schedule = PWRReactorSchedule.create()
}
