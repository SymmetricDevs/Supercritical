package io.github.symmetricdevs.supercritical.api.fission.ecs.resources

import io.github.symmetricdevs.supercritical.api.fission.reactor.ReactorCore
import io.github.symmetricdevs.supercritical.api.fission.ecs.Resource

/**
 * Resource that holds the owning [ReactorCore] so systems can reach legacy hooks
 * during the incremental migration from kernels to systems.
 */
class ReactorCoreResource(val core: ReactorCore) : Resource
