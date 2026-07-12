package io.github.symmetricdevs.supercritical.core.mixin.gregtech;

import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import org.spongepowered.asm.mixin.Mixin;
import io.github.symmetricdevs.supercritical.api.items.armor.ArmorLogicExtension;

/**
 * Injects {@link ArmorLogicExtension} into GTCEu's armor logic interface.
 */
@Mixin(value = IArmorLogic.class, remap = false)
public interface MixinIArmorLogic extends ArmorLogicExtension {
}
