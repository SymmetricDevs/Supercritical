package io.github.symmetricdevs.supercritical.core.mixin.gregtech;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.symmetricdevs.supercritical.data.recipe.ScritRecipeUtils;

@Mixin(value = GTRecipeType.class, remap = false)
public class MixinGTRecipeType {

    @Inject(method = "beginStagingRecipes", at = @At("TAIL"))
    private void sc$addDeferredRecipes(CallbackInfo ci) {
        ScritRecipeUtils.INSTANCE.addDeferredRecipes((GTRecipeType) (Object) this);
    }
}
