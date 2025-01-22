package supercritical.api.unification.ore;

import java.util.function.Function;

import gregtech.api.unification.ore.OrePrefix;
import supercritical.mixins.gregtech.MixinOrePrefix;

/**
 * Extends {@link OrePrefix} to include radiation properties.
 * Also see {@link MixinOrePrefix} and {@link SCOrePrefix}.
 */
public interface OrePrefixExtension {

    Function<Double, Double> getRadiationDamageFunction();

    void setRadiationDamageFunction(Function<Double, Double> function);
}
