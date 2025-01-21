package gregicality.nuclear.api.unification.ore;

import java.util.function.Function;

import gregicality.nuclear.mixins.gregtech.MixinOrePrefix;
import gregtech.api.unification.ore.OrePrefix;

/**
 * Extends {@link OrePrefix} to include radiation properties.
 * Also see {@link MixinOrePrefix} and {@link GCYNOrePrefix}.
 */
public interface OrePrefixExtension {

    Function<Double, Double> getRadiationDamageFunction();

    void setRadiationDamageFunction(Function<Double, Double> function);
}
