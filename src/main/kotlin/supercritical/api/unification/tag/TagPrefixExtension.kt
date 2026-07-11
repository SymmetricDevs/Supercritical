package supercritical.api.unification.tag

import com.gregtechceu.gtceu.api.data.tag.TagPrefix

/**
 * Extends GTCEu Modern's [TagPrefix] to include
 * radiation damage behaviour for Supercritical nuclear items.
 */
typealias damageFunction = java.util.function.Function<Double, Double>

interface TagPrefixExtension {

    var radiationDamageFunction: damageFunction

    companion object {
        fun getRadiationDamageFunction(prefix: TagPrefix): damageFunction {
            return (prefix as Any? as TagPrefixExtension).radiationDamageFunction
        }

        fun setRadiationDamageFunction(
            prefix: TagPrefix,
            function: damageFunction
        ) {
            (prefix as Any? as TagPrefixExtension).radiationDamageFunction = function
        }
    }
}
