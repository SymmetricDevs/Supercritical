package supercritical.core.mixin.gregtech

import com.gregtechceu.gtceu.api.data.chemical.Element
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import supercritical.api.unification.ElementExtension
import supercritical.api.unification.material.MaterialExtension
import kotlin.math.exp
import kotlin.math.ln

@Mixin(value = [Material::class], remap = false)
abstract class MixinMaterial : MaterialExtension {
    @get:Shadow
    abstract val element: Element?

    @get:Shadow
    abstract val isRadioactive: Boolean

    @get:Shadow
    abstract val materialComponents: ImmutableList<MaterialStack>?

    override fun getDecaysPerSecond(): Double {
        if (!this.isRadioactive) {
            return 0.0
        }
        val element = this.element
        if (element != null) {
            val halfLife = (element as ElementExtension).getHalfLifeSeconds()
            if (halfLife > 0) {
                return 6e23 * (ln(2.0) * exp(-ln(2.0) / halfLife))
            }
            return 0.0
        }
        var decaysPerSecond = 0.0
        for (stack in this.materialComponents) {
            decaysPerSecond += stack.amount() * MaterialExtension.Companion.getDecaysPerSecond(stack.material())
        }
        return decaysPerSecond
    }

    @Inject(method = ["isRadioactive"], at = [At(value = "RETURN", ordinal = 0)], cancellable = true)
    fun isActuallyRadioactive(cir: CallbackInfoReturnable<Boolean?>) {
        val element = this.element
        if (element != null) {
            cir.setReturnValue((element as ElementExtension).getHalfLifeSeconds() >= 0)
        }
    }
}
