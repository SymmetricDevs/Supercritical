package io.github.symmetricdevs.supercritical.api.fission.ecs.resources

import io.github.symmetricdevs.supercritical.api.fission.ecs.Entity
import io.github.symmetricdevs.supercritical.api.fission.ecs.Resource

/**
 * Resource holding the reactor root entity. Systems use this to access global
 * components without needing to know the entity id convention.
 */
class RootEntityResource(val entity: Entity) : Resource
