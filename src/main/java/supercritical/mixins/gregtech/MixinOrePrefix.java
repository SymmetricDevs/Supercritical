package supercritical.mixins.gregtech;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import gregtech.api.unification.ore.OrePrefix;
import supercritical.api.unification.ore.OrePrefixExtension;

@Mixin(value = OrePrefix.class, remap = false)
public abstract class MixinOrePrefix implements OrePrefixExtension {

    @Unique
    public Function<Double, Double> sc$radiationDamageFunction = null;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public Function<Double, Double> getRadiationDamageFunction() {
        return sc$radiationDamageFunction;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public void setRadiationDamageFunction(Function<Double, Double> function) {
        this.sc$radiationDamageFunction = function;
    }
}
