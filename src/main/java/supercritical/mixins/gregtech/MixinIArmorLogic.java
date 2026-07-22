package supercritical.mixins.gregtech;

import gregtech.api.items.armor.IArmorLogic;
import org.spongepowered.asm.mixin.Mixin;
import supercritical.api.items.armor.ArmorLogicExtension;

/**
 * No additional functionality is added here. Used solely to inject the parent
 * interface {@link ArmorLogicExtension}.
 */
@Mixin(value = IArmorLogic.class, remap = false)
public interface MixinIArmorLogic extends ArmorLogicExtension {

}
