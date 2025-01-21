package gregicality.nuclear.mixins.gregtech;

import org.spongepowered.asm.mixin.Mixin;

import gregicality.nuclear.api.items.armor.ArmorLogicExtension;
import gregtech.api.items.armor.IArmorLogic;

/**
 * No additional functionality is added here.
 * Used solely to inject the parent interface {@link ArmorLogicExtension}.
 */
@Mixin(value = IArmorLogic.class, remap = false)
public interface MixinIArmorLogic extends ArmorLogicExtension {

}
