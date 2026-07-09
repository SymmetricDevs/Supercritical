package supercritical.api.unification.tag

import com.gregtechceu.gtceu.api.data.tag.TagPrefix

/**
 * Extends GTCEu Modern's [TagPrefix] to include
 * radiation damage behaviour for Supercritical nuclear items.
 */
interface TagPrefixExtension {
    var radiationDamageFunction: Function<Double?, Double?>?

    companion object {
        fun getRadiationDamageFunction(prefix: TagPrefix): java.util.function.Function<Double?, Double?>? {
            return (prefix as Any? as TagPrefixExtension).radiationDamageFunction
        }

        fun setRadiationDamageFunction(
            prefix: TagPrefix,
            function: java.util.function.Function<Double?, Double?>?
        ) {
            (prefix as Any? as TagPrefixExtension).radiationDamageFunction = function
        }
    }
}
