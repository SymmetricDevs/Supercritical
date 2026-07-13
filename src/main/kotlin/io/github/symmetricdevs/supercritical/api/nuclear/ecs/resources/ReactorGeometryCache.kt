package io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Entity
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Resource

/**
 * Cached reactor geometry, produced by [GeometryRebuildSystem] and consumed by the
 * neutronics / thermal systems.
 *
 * Each list holds the cell entities of one component kind in stable row-major
 * (x-major, then y) order — the same order the cell entities are created by the
 * setControlRod / setModerator / setFuelRod / setCoolantChannel methods, so eigenvalue
 * matrix indices and per-rod weight assignment stay deterministic and match the pinned
 * regression values. Systems read/write the live component data on these entities directly;
 * the cache only stores the entity references and the effective-control-rod subset.
 */
class ReactorGeometryCache : Resource {
    val fuelRods = ArrayList<Entity>()
    val controlRods = ArrayList<Entity>()
    val coolantChannels = ArrayList<Entity>()
    val effectiveControlRods = ArrayList<Entity>()

    fun clear() {
        fuelRods.clear()
        controlRods.clear()
        coolantChannels.clear()
        effectiveControlRods.clear()
    }
}
