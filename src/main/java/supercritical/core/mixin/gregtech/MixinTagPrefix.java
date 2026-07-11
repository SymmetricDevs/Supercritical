package supercritical.core.mixin.gregtech;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import supercritical.api.unification.tag.TagPrefixExtension;

import java.util.function.Function;

@Mixin(value = TagPrefix.class, remap = false)
public abstract class MixinTagPrefix implements TagPrefixExtension {

    @Unique
    private Function<Double, Double> sc$radiationDamageFunction;

    @Override
    public Function<Double, Double> getRadiationDamageFunction() {
        return sc$radiationDamageFunction;
    }

    @Override
    public void setRadiationDamageFunction(Function<Double, Double> function) {
        sc$radiationDamageFunction = function;
    }
}
