package io.github.symmetricdevs.supercritical.api.nuclear.reactor.families

import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorFamily
import net.minecraft.resources.ResourceLocation

object LegacyPWRFamily : ReactorFamily {
    override val id: ResourceLocation = ResourceLocation.fromNamespaceAndPath("supercritical", "pwr")
}
