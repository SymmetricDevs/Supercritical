package io.github.symmetricdevs.supercritical.api.fission.ecs.components

import io.github.symmetricdevs.supercritical.api.fission.ecs.Component

/**
 * Lattice geometry mapping.
 *
 * Holds the reactor size, depth, and a flat `IntArray` that maps each lattice
 * position to the index of the cell entity at that position, or -1 if empty.
 *
 * **Neighbor traversal** is done via coordinate math in the physics systems
 * (e.g. `pos.x + dx[i]`, `pos.y + dy[i]` with a size-keyed index lookup),
 * because the thermal and neutronics systems need grid-coordinate → entity-key
 * resolution rather than entity → neighbor-entity traversal. There is no
 * standalone `neighbors()` accessor — each system's traversal is tuned to its
 * data structures and adding a shared helper wouldn't reduce duplication.
 */
@JvmRecord
data class LatticeGeometryComponent(
    val size: Int,
    val depth: Int,
    val cellEntities: IntArray
) : Component {
    init {
        require(cellEntities.size == size * size) {
            "cellEntities size ${cellEntities.size} != $size * $size"
        }
    }

    fun entityIndexAt(x: Int, y: Int): Int {
        if (x !in 0 until size || y !in 0 until size) return -1
        return cellEntities[x * size + y]
    }

    fun setEntityIndexAt(x: Int, y: Int, entityIndex: Int) {
        if (x in 0 until size && y in 0 until size) {
            cellEntities[x * size + y] = entityIndex
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LatticeGeometryComponent) return false
        return size == other.size && depth == other.depth && cellEntities.contentEquals(other.cellEntities)
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + depth
        result = 31 * result + cellEntities.contentHashCode()
        return result
    }
}
