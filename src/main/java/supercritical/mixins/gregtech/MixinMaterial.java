package supercritical.mixins.gregtech;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supercritical.api.unification.ElementExtension;
import supercritical.api.unification.material.MaterialExtension;
import supercritical.api.unification.material.properties.SCPropertyKey;

@Mixin(value = Material.class, remap = false)
public abstract class MixinMaterial implements MaterialExtension {

    @Shadow
    @Nullable
    public abstract Element getElement();

    @Shadow
    public abstract boolean isRadioactive();

    @Shadow
    public abstract ImmutableList<MaterialStack> getMaterialComponents();

    @Override
    public double getDecaysPerSecond() {
        if (!isRadioactive()) {
            return 0;
        }
        Element element = getElement();
        if (element != null) {
            double halfLife = ((ElementExtension) element).getHalfLifeSeconds();
            if (halfLife > 0) {
                return 6e23 * (Math.log(2) * Math.exp(-Math.log(2) / halfLife));
            }
            return 0;
        }
        double decaysPerSecond = 0;
        for (MaterialStack stack : getMaterialComponents()) {
            decaysPerSecond += stack.amount() * MaterialExtension.getDecaysPerSecond(stack.material());
        }
        return decaysPerSecond;
    }

    @Inject(method = "isRadioactive", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    public void isActuallyRadioactive(CallbackInfoReturnable<Boolean> cir) {
        Element element = getElement();
        if (element != null) {
            cir.setReturnValue(((ElementExtension) element).getHalfLifeSeconds() >= 0);
        }
    }
}
