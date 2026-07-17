package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.reactor.ReactorFamily
import io.github.symmetricdevs.supercritical.api.fission.ecs.Component

/**
 * Tags the reactor root entity with its family.
 */
@JvmRecord
data class ReactorFamilyComponent(
    val family: ReactorFamily
) : Component
