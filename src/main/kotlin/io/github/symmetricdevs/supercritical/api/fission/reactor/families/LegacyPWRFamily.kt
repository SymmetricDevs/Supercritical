package io.github.symmetricdevs.supercritical.api.fission.reactor.families

import io.github.symmetricdevs.supercritical.api.fission.reactor.ReactorFamily
import io.github.symmetricdevs.supercritical.api.fission.reactor.pwr.PWRSchedule
import io.github.symmetricdevs.supercritical.api.fission.ecs.Schedule
import io.github.symmetricdevs.supercritical.api.fission.ecs.registration.ComponentTypeRegistry
import net.minecraft.resources.ResourceLocation

object LegacyPWRFamily : ReactorFamily {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("supercritical", "pwr")

    override fun registerComponents(registry: ComponentTypeRegistry) {
        io.github.symmetricdevs.supercritical.api.fission.ecs.components.ReactorComponentTypes.registerAll(registry)
    }

    override fun buildSchedule(): Schedule = PWRSchedule.create()
}
