package io.github.symmetricdevs.supercritical.api.fission.reactor.systems

import io.github.symmetricdevs.supercritical.api.fission.ecs.World
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.ReactorComponentTypes
import io.github.symmetricdevs.supercritical.api.fission.ecs.resources.ReactorGeometryCache

/**
 * Combined reactivity contribution of the effective control-rod bank at [insertion].
 * Ported verbatim from `ControlRod.controlRodFactor`; shared by the eigenvalue precompute
 * (which seeds `controlRodFactor` after sizing the effective bank) and the per-tick
 * regulator (which re-evaluates it after nudging the insertion).
 */
internal fun controlRodFactor(world: World, cache: ReactorGeometryCache, insertion: Double): Double {
    var factor = 0.0
    for (entity in cache.effectiveControlRods) {
        val rod = world.getComponent(entity, ReactorComponentTypes.CONTROL_ROD)!!
        factor += if (rod.tipModeration) {
            if (insertion <= 0.3) {
                -(insertion / 3 * rod.weight)
            } else {
                -(-11.0 / 7 * (insertion - 0.3) + 0.1) * rod.weight
            }
        } else {
            insertion * rod.weight
        }
    }
    return factor
}
