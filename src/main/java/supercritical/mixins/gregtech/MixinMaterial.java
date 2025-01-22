package supercritical.mixins.gregtech;

import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import supercritical.api.unification.ElementExtension;
import supercritical.api.unification.material.MaterialExtension;

@Mixin(value = Material.class, remap = false)
public abstract class MixinMaterial implements MaterialExtension {

    /**
     * Cached here to avoid repeated reflection calls.
     */
    @Unique
    private MaterialInfoAccessor sc$info;

    @Shadow
    public abstract boolean isRadioactive();

    /**
     * Work-around for the fact that MaterialInfo is private.
     * Provides access to MaterialInfo by using reflection.
     */
    @Unique
    public MaterialInfoAccessor sc$info() {
        if (sc$info == null) {
            try {
                Field field = Material.class.getDeclaredField("materialInfo");
                field.setAccessible(true);
                sc$info = (MaterialInfoAccessor) field.get(this);
                field.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }
        return sc$info;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public double getDecaysPerSecond() {
        if (!this.isRadioactive()) {
            return 0;
        }
        if (sc$info().getElement() != null) {
            return 6e23 * (Math.log(2) *
                    Math.exp(-Math.log(2) / ((ElementExtension) sc$info().getElement()).getHalfLiveSeconds()));
        }
        double decaysPerSecond = 0;
        for (MaterialStack stack : sc$info().getComponentList())
            decaysPerSecond += ((MaterialExtension) stack.material).getDecaysPerSecond();
        return decaysPerSecond;
    }

    @Inject(method = "isRadioactive", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    public void isActuallyRadioactive(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(((ElementExtension) sc$info().getElement()).getHalfLiveSeconds() >= 0);
    }
}
