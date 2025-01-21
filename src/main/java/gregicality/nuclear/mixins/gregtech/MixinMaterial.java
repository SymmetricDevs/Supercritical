package gregicality.nuclear.mixins.gregtech;

import gregicality.nuclear.api.unification.ElementExtension;
import gregicality.nuclear.api.unification.material.MaterialExtension;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(value = Material.class, remap = false)
public abstract class MixinMaterial implements MaterialExtension {

    /**
     * Cached here to avoid repeated reflection calls.
     */
    @Unique
    private MaterialInfoAccessor gcyn$info;

    @Shadow
    public abstract boolean isRadioactive();

    /**
     * Work-around for the fact that MaterialInfo is private.
     * Provides access to MaterialInfo by using reflection.
     */
    @Unique
    public MaterialInfoAccessor gcyn$info() {
        if (gcyn$info == null) {
            try {
                Field field = Material.class.getDeclaredField("materialInfo");
                field.setAccessible(true);
                gcyn$info = (MaterialInfoAccessor) field.get(this);
                field.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        return gcyn$info;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public double getDecaysPerSecond() {
        if (!this.isRadioactive()) {
            return 0;
        }
        if (gcyn$info().getElement() != null) {
            return 6e23 * (Math.log(2) * Math.exp(-Math.log(2) / ((ElementExtension) gcyn$info().getElement()).getHalfLiveSeconds()));
        }
        double decaysPerSecond = 0;
        for (MaterialStack stack : gcyn$info().getComponentList())
            decaysPerSecond += ((MaterialExtension) stack.material).getDecaysPerSecond();
        return decaysPerSecond;
    }

    @Inject(method = "isRadioactive", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    public void isActuallyRadioactive(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(((ElementExtension) gcyn$info().getElement()).getHalfLiveSeconds() >= 0);
    }
}
