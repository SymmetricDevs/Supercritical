package supercritical.api.unification.tag;

import java.util.function.Function;

/**
 * Extends GTCEu Modern's {@link com.gregtechceu.gtceu.api.data.tag.TagPrefix} to include
 * radiation damage behaviour for Supercritical nuclear items.
 */
public interface TagPrefixExtension {

    Function<Double, Double> getRadiationDamageFunction();

    void setRadiationDamageFunction(Function<Double, Double> function);

    static Function<Double, Double> getRadiationDamageFunction(com.gregtechceu.gtceu.api.data.tag.TagPrefix prefix) {
        return ((TagPrefixExtension) (Object) prefix).getRadiationDamageFunction();
    }

    static void setRadiationDamageFunction(com.gregtechceu.gtceu.api.data.tag.TagPrefix prefix,
                                           Function<Double, Double> function) {
        ((TagPrefixExtension) (Object) prefix).setRadiationDamageFunction(function);
    }
}
