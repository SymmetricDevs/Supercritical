package io.github.symmetricdevs.supercritical.api.nuclear.reactor

import net.minecraft.resources.ResourceLocation

/** Family discriminator. Open, not sealed, so addon mods can register new families. */
interface ReactorFamily {
    val id: ResourceLocation
}
