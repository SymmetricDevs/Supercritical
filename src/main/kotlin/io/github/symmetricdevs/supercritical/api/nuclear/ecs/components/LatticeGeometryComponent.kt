package io.github.symmetricdevs.supercritical.api.nuclear.ecs.components

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.Component

/**
 * Lattice geometry mapping.
 *
 * Holds the reactor size, depth, and a flat `IntArray` that maps each lattice
 * position to the index of the cell entity at that position, or -1 if empty.
 */
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

    fun neighbors(entityIndex: Int): IntArray {
        val x = entityIndex / size
        val y = entityIndex % size
        val result = IntArray(4) { -1 }
        if (x > 0) result[0] = cellEntities[(x - 1) * size + y]
        if (y < size - 1) result[1] = cellEntities[x * size + (y + 1)]
        if (x < size - 1) result[2] = cellEntities[(x + 1) * size + y]
        if (y > 0) result[3] = cellEntities[x * size + (y - 1)]
        return result
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
