package gregicality.nuclear.mixins.gregtech;

import gregicality.nuclear.api.cover.ICustomEnergyCover;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.common.covers.detector.CoverDetectorBase;
import gregtech.common.covers.detector.CoverDetectorEnergy;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CoverDetectorEnergy.class, remap = false)
public abstract class MixinCoverDetectorEnergy extends CoverDetectorBase {

    public MixinCoverDetectorEnergy(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                    @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Inject(method = "canAttach", at = @At("HEAD"), cancellable = true)
    public void iThinkICan(CoverableView coverable, EnumFacing why_should_I_care, CallbackInfoReturnable<Boolean> cir) {
        if (coverable instanceof ICustomEnergyCover) cir.setReturnValue(true);
    }

    @Inject(method = "getCoverHolderCapacity", at = @At("HEAD"), cancellable = true)
    public void getCustomCapacity(CallbackInfoReturnable<Long> cir) {
        if (getCoverableView() instanceof ICustomEnergyCover custom) cir.setReturnValue(custom.getCoverCapacity());
    }

    @Inject(method = "getCoverHolderStored", at = @At("HEAD"), cancellable = true)
    public void getCustomStored(CallbackInfoReturnable<Long> cir) {
        if (getCoverableView() instanceof ICustomEnergyCover custom) cir.setReturnValue(custom.getCoverStored());
    }
}
