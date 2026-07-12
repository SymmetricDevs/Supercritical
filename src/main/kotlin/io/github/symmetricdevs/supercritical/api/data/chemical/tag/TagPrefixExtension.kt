package io.github.symmetricdevs.supercritical.api.data.chemical.tag

import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import java.util.function.Function

/**
 * Extends GTCEu Modern's [TagPrefix] to include
 * radiation damage behavior for Supercritical nuclear items.
 */
typealias DamageFunction = Function<Double, Double>

interface TagPrefixExtension {

    var radiationDamageFunction: DamageFunction?

    companion object {

        var TagPrefix.radiationDamageFunction: DamageFunction?
            set(value) {
                (this as TagPrefixExtension).radiationDamageFunction = value
            }
            get() = (this as TagPrefixExtension).radiationDamageFunction
    }
}
