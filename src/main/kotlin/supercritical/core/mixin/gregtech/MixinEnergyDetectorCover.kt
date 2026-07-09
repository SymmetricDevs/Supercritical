package supercritical.core.mixin.gregtech

import com.gregtechceu.gtceu.api.capability.ICoverable
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider.EnergyInfo
import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.common.cover.detector.DetectorCover
import com.gregtechceu.gtceu.common.cover.detector.EnergyDetectorCover
import net.minecraft.core.Direction
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import supercritical.api.cover.ICustomEnergyCover
import java.math.BigInteger

/**
 * Allows energy detector covers to attach to blocks implementing [ICustomEnergyCover].
 */
@Mixin(value = [EnergyDetectorCover::class], remap = false)
abstract class MixinEnergyDetectorCover(definition: CoverDefinition, coverHolder: ICoverable, attachedSide: Direction) :
    DetectorCover(definition, coverHolder, attachedSide) {
    @Inject(method = ["canAttach"], at = [At("HEAD")], cancellable = true)
    fun `sc$canAttachToCustomCover`(cir: CallbackInfoReturnable<Boolean?>) {
        if (coverHolder is ICustomEnergyCover) {
            cir.setReturnValue(true)
        }
    }

    @Inject(method = ["getEnergyInfoProvider"], at = [At("HEAD")], cancellable = true)
    protected fun `sc$getCustomEnergyInfoProvider`(cir: CallbackInfoReturnable<IEnergyInfoProvider?>) {
        if (coverHolder is ICustomEnergyCover) {
            cir.setReturnValue(object : IEnergyInfoProvider {
                override fun getEnergyInfo(): EnergyInfo {
                    return EnergyInfo(
                        BigInteger.valueOf(coverHolder.getCoverCapacity()),
                        BigInteger.valueOf(coverHolder.getCoverStored())
                    )
                }

                override fun getInputPerSec(): Long {
                    return 0
                }

                override fun getOutputPerSec(): Long {
                    return 0
                }

                override fun supportsBigIntEnergyValues(): Boolean {
                    return false
                }
            })
        }
    }
}
