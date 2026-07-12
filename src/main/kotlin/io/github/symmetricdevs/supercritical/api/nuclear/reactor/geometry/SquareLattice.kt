package io.github.symmetricdevs.supercritical.api.nuclear.reactor.geometry

import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.ReactorComponent
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorGeometry
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.families.LegacyPWRFamily

class SquareLattice(
    val size: Int,
    val depth: Int,
    private val cells: Array<Array<ReactorComponent?>>
) : ReactorGeometry {
    override val family = LegacyPWRFamily
    override val nodeCount: Int get() = size * size

    fun componentAt(r: Int, c: Int): ReactorComponent? =
        if (r in 0..<size && c in 0..<size) cells[r][c] else null

    fun neighbors(node: Int): IntArray {
        val r = node / size
        val c = node % size
        val result = IntArray(4) { -1 }
        if (r > 0) result[0] = node - size
        if (c < size - 1) result[1] = node + 1
        if (r < size - 1) result[2] = node + size
        if (c > 0) result[3] = node - 1
        return result
    }

    override fun invalidate() {
        // No caches to clear at this layer.
    }
}
