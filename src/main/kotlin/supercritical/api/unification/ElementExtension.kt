package supercritical.api.unification;

import com.gregtechceu.gtceu.api.data.chemical.Element;

/**
 * Extends {@link Element} with a double-precision half-life in seconds.
 * The modern Element class stores half-life as a long; this mixin converts
 * to a double so Supercritical can represent sub-second isotope lifetimes.
 */
public interface ElementExtension {

    double getHalfLifeSeconds();

    void setHalfLifeSeconds(double halfLifeSeconds);

    /**
     * Convenience helper for legacy callers that expect a long value.
     */
    default long getHalfLifeSecondsLong() {
        return (long) getHalfLifeSeconds();
    }
}
