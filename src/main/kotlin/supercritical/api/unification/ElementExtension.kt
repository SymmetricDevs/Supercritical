package supercritical.api.unification

/**
 * Extends [Element] with a double-precision half-life in seconds.
 * The modern Element class stores half-life as a long; this mixin converts
 * to a double so Supercritical can represent sub-second isotope lifetimes.
 */
interface ElementExtension {
    var halfLifeSeconds: Double

    val halfLifeSecondsLong: Long
        /**
         * Convenience helper for legacy callers that expect a long value.
         */
        get() = this.halfLifeSeconds.toLong()
}
