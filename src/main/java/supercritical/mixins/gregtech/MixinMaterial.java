package supercritical.mixins.gregtech;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;

import gregtech.api.unification.Element;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import supercritical.api.unification.ElementExtension;
import supercritical.api.unification.material.MaterialExtension;

@Mixin(value = Material.class, remap = false)
public abstract class MixinMaterial implements MaterialExtension {

    @Shadow
    @Nullable
    public abstract Element getElement();

    @Shadow
    public abstract boolean isRadioactive();

    @Shadow
    public abstract ImmutableList<MaterialStack> getMaterialComponents();

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public double getDecaysPerSecond() {
        if (!isRadioactive()) {
            return 0;
        }
        if (getElement() != null) {
            return 6e23 * (Math.log(2) *
                    Math.exp(-Math.log(2) / ((ElementExtension) getElement()).getHalfLiveSeconds()));
        }
        double decaysPerSecond = 0;
        for (MaterialStack stack : getMaterialComponents())
            decaysPerSecond += ((MaterialExtension) stack.material).getDecaysPerSecond();
        return decaysPerSecond;
    }

    @Inject(method = "isRadioactive", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    public void isActuallyRadioactive(CallbackInfoReturnable<Boolean> cir) {
        // noinspection DataFlowIssue
        cir.setReturnValue(((ElementExtension) getElement()).getHalfLiveSeconds() >= 0);
    }
}
