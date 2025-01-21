package gregicality.nuclear.mixins.gregtech;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import gregicality.nuclear.api.unification.ore.OrePrefixExtension;
import gregtech.api.unification.ore.OrePrefix;

@Mixin(value = OrePrefix.class, remap = false)
public abstract class MixinOrePrefix implements OrePrefixExtension {

    @Unique
    public Function<Double, Double> gcyn$radiationDamageFunction = null;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public Function<Double, Double> getRadiationDamageFunction() {
        return gcyn$radiationDamageFunction;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public void setRadiationDamageFunction(Function<Double, Double> function) {
        this.gcyn$radiationDamageFunction = function;
    }
}
