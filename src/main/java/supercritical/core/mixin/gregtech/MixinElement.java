package supercritical.core.mixin.gregtech;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import supercritical.api.data.chemical.ElementExtension;

/**
 * Adds a double-precision half-life to {@link Element} while keeping the original long value intact.
 */
@Mixin(value = Element.class, remap = false)
public abstract class MixinElement implements ElementExtension {

    @Unique
    private double sc$halfLifeSeconds = -1.0;

    @Override
    public double getHalfLifeSeconds() {
        return sc$halfLifeSeconds;
    }

    @Override
    public void setHalfLifeSeconds(double halfLifeSeconds) {
        sc$halfLifeSeconds = halfLifeSeconds;
    }
}
