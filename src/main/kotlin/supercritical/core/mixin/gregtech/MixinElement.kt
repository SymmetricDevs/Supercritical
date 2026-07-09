package supercritical.core.mixin.gregtech

import com.gregtechceu.gtceu.api.data.chemical.Element
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import supercritical.api.unification.ElementExtension

/**
 * Adds a double-precision half-life to [Element] while keeping the original long value intact.
 */
@Mixin(value = [Element::class], remap = false)
abstract class MixinElement : ElementExtension {
    @Unique
    private var `sc$halfLifeSeconds` = -1.0

    override fun getHalfLifeSeconds(): Double {
        return `sc$halfLifeSeconds`
    }

    override fun setHalfLifeSeconds(halfLifeSeconds: Double) {
        `sc$halfLifeSeconds` = halfLifeSeconds
    }
}
