package supercritical.core.mixin.gregtech;

import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.common.cover.detector.DetectorCover;
import com.gregtechceu.gtceu.common.cover.detector.EnergyDetectorCover;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supercritical.api.cover.ICustomEnergyCover;

import java.math.BigInteger;

/**
 * Allows energy detector covers to attach to blocks implementing {@link ICustomEnergyCover}.
 */
@Mixin(value = EnergyDetectorCover.class, remap = false)
public abstract class MixinEnergyDetectorCover extends DetectorCover {

    public MixinEnergyDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Inject(method = "canAttach", at = @At("HEAD"), cancellable = true)
    public void sc$canAttachToCustomCover(CallbackInfoReturnable<Boolean> cir) {
        if (coverHolder instanceof ICustomEnergyCover) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getEnergyInfoProvider", at = @At("HEAD"), cancellable = true)
    protected void sc$getCustomEnergyInfoProvider(CallbackInfoReturnable<IEnergyInfoProvider> cir) {
        if (coverHolder instanceof ICustomEnergyCover custom) {
            cir.setReturnValue(new IEnergyInfoProvider() {
                @Override
                public EnergyInfo getEnergyInfo() {
                    return new EnergyInfo(BigInteger.valueOf(custom.getCoverCapacity()),
                            BigInteger.valueOf(custom.getCoverStored()));
                }

                @Override
                public long getInputPerSec() {
                    return 0;
                }

                @Override
                public long getOutputPerSec() {
                    return 0;
                }

                @Override
                public boolean supportsBigIntEnergyValues() {
                    return false;
                }
            });
        }
    }
}
