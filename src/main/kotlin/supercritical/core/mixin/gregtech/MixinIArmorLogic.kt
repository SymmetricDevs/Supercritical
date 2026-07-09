package supercritical.core.mixin.gregtech

import com.gregtechceu.gtceu.api.item.armor.IArmorLogic
import org.spongepowered.asm.mixin.Mixin
import supercritical.api.items.armor.ArmorLogicExtension

/**
 * Injects [ArmorLogicExtension] into GTCEu's armor logic interface.
 */
@Mixin(value = [IArmorLogic::class], remap = false)
interface MixinIArmorLogic : ArmorLogicExtension
