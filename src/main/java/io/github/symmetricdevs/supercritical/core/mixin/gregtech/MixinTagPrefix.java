package io.github.symmetricdevs.supercritical.core.mixin.gregtech;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import io.github.symmetricdevs.supercritical.api.data.chemical.tag.TagPrefixExtension;

import java.util.function.Function;

@Mixin(value = TagPrefix.class, remap = false)
public abstract class MixinTagPrefix implements TagPrefixExtension {

    @Nullable
    @Unique
    private Function<Double, Double> sc$radiationDamageFunction;

    @Nullable
    @Override
    public Function<Double, Double> getRadiationDamageFunction() {
        return sc$radiationDamageFunction;
    }

    @Override
    public void setRadiationDamageFunction(@Nullable Function<Double, Double> function) {
        sc$radiationDamageFunction = function;
    }
}
