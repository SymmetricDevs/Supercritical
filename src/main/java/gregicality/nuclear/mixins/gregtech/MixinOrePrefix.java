package gregicality.nuclear.mixins.gregtech;

import gregicality.nuclear.api.unification.ore.OrePrefixExtension;
import gregtech.api.unification.ore.OrePrefix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

@Mixin(value = OrePrefix.class, remap = false)
public abstract class MixinOrePrefix implements OrePrefixExtension {

    @Unique
    public Function<Double, Double> gcyn$radiationDamageFunction = null;


    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public Function<Double, Double> getDamageFunction() {
        return gcyn$radiationDamageFunction;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public void setDamageFunction(Function<Double, Double> function) {
        this.gcyn$radiationDamageFunction = function;
    }
}
