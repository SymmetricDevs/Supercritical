package gregicality.nuclear.mixins.gregtech;

import gregicality.nuclear.api.unification.ElementExtension;
import gregtech.api.unification.Element;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Element.class, remap = false)
public abstract class MixinElement implements ElementExtension {

    @Unique
    public double gcyn$halfLifeSeconds = -1.;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public double getHalfLiveSeconds() {
        return gcyn$halfLifeSeconds;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public void setHalfLiveSeconds(double halfLifeSeconds) {
        gcyn$halfLifeSeconds = halfLifeSeconds;
    }
}
