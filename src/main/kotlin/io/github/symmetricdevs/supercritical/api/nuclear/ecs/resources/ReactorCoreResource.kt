package io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Resource
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorCore

/**
 * Resource that holds the owning [ReactorCore] so systems can reach legacy hooks
 * during the incremental migration from kernels to systems.
 */
class ReactorCoreResource(val core: ReactorCore) : Resource
