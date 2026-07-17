package io.github.symmetricdevs.supercritical.api.fission.ecs

/**
 * Packed entity identifier local to one [io.github.symmetricdevs.supercritical.api.fission.ecs.World].
 *
 * Layout:
 * - bits 0..23 : entity index in the world's dense entity array
 * - bits 24..31: generation counter (0..255), used to detect stale references
 * - bits 32..63: reserved (zero)
 */
@JvmInline
value class Entity(val bits: Long) {

    val index: Int get() = (bits and INDEX_MASK).toInt()
    val generation: Int get() = ((bits shr GENERATION_SHIFT) and GENERATION_MASK).toInt()
    val isValid: Boolean get() = bits != INVALID.bits

    constructor(index: Int, generation: Int) : this(
        (index.toLong() and INDEX_MASK) or
            ((generation.toLong() and GENERATION_MASK) shl GENERATION_SHIFT)
    )

    companion object {
        private const val INDEX_MASK = 0x00FFFFFFL
        private const val GENERATION_MASK = 0xFFL
        private const val GENERATION_SHIFT = 24

        /** Sentinel used by storages to mark an unused slot. */
        val INVALID = Entity(-1L)
    }

    override fun toString(): String = "Entity(index=$index, generation=$generation)"
}
