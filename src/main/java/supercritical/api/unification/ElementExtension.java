package supercritical.api.unification;

import gregtech.api.unification.Element;
import lombok.experimental.UtilityClass;

/// See [MixinElement]
public interface ElementExtension {

    double getHalfLiveSeconds();

    void setHalfLiveSeconds(double halfLifeSeconds);

    @UtilityClass
    class Handler {

        public double getHalfLiveSeconds(Element element) {
            return ((ElementExtension) element).getHalfLiveSeconds();
        }

        public void setHalfLiveSeconds(Element element, double halfLifeSeconds) {
            ((ElementExtension) element).setHalfLiveSeconds(halfLifeSeconds);
        }
    }
}
