package gregicality.nuclear.api.unification.ore;

import gregicality.nuclear.mixins.gregtech.MixinOrePrefix;
import gregtech.api.unification.ore.OrePrefix;

import java.util.function.Function;

/**
 * Extends {@link OrePrefix} to include radiation properties.
 * Also see {@link MixinOrePrefix} and {@link GCYNOrePrefix}.
 */
public interface OrePrefixExtension {

    Function<Double, Double> getRadiationDamageFunction();

    void setRadiationDamageFunction(Function<Double, Double> function);
}
