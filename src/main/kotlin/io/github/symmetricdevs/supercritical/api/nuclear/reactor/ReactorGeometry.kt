package io.github.symmetricdevs.supercritical.api.nuclear.reactor

/** Minimal topology contract. Rich interpretations are exposed via typed accessors. */
interface ReactorGeometry {
    val family: ReactorFamily
    val nodeCount: Int

    fun invalidate()

    @Suppress("UNCHECKED_CAST")
    fun <T : ReactorGeometry> typed(): T? = this as? T
}
