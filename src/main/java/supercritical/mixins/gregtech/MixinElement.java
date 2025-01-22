package supercritical.mixins.gregtech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import gregtech.api.unification.Element;
import supercritical.api.unification.ElementExtension;

/**
 * Uses {@link ElementExtension} to add a half-life value in seconds to {@link Element},
 * replacing the original long value, while maintaining (some sort of) compatibility.
 */
@Mixin(value = Element.class, remap = false)
public abstract class MixinElement implements ElementExtension {

    @Unique
    public double sc$halfLifeSeconds = -1.;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public double getHalfLiveSeconds() {
        return sc$halfLifeSeconds;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public void setHalfLiveSeconds(double halfLifeSeconds) {
        sc$halfLifeSeconds = halfLifeSeconds;
    }
}
