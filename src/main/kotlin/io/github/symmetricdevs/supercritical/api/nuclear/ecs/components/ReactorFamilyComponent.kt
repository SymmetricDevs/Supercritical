package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorFamily

/**
 * Tags the reactor root entity with its family.
 */
data class ReactorFamilyComponent(
    val family: ReactorFamily
) : Component
