package supercritical.core.mixin.gregtech;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supercritical.loaders.recipe.SCRecipeUtils;

@Mixin(value = GTRecipeType.class, remap = false)
public class MixinGTRecipeType {

    @Inject(method = "beginStagingRecipes", at = @At("TAIL"))
    private void sc$addDeferredRecipes(CallbackInfo ci) {
        SCRecipeUtils.addDeferredRecipes((GTRecipeType) (Object) this);
    }
}
