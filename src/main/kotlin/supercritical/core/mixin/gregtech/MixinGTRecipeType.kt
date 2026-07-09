package supercritical.core.mixin.gregtech

import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import supercritical.loaders.recipe.SCRecipeUtils

@Mixin(value = [GTRecipeType::class], remap = false)
class MixinGTRecipeType {
    @Inject(method = ["beginStagingRecipes"], at = [At("TAIL")])
    private fun `sc$addDeferredRecipes`(ci: CallbackInfo?) {
        SCRecipeUtils.addDeferredRecipes(this as Any as GTRecipeType)
    }
}
