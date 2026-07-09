package supercritical.core.mixin.gregtech

import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import supercritical.api.unification.tag.TagPrefixExtension
import java.util.function.Function

@Mixin(value = [TagPrefix::class], remap = false)
abstract class MixinTagPrefix : TagPrefixExtension {
    @Unique
    private var `sc$radiationDamageFunction`: Function<Double?, Double?>? = null

    override fun getRadiationDamageFunction(): Function<Double?, Double?>? {
        return `sc$radiationDamageFunction`
    }

    override fun setRadiationDamageFunction(function: Function<Double?, Double?>?) {
        `sc$radiationDamageFunction` = function
    }
}
