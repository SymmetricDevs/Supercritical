package gregicality.nuclear.mixins.gregtech;

import gregicality.nuclear.api.items.armor.IRadiationResistanceArmorLogic;
import gregtech.api.items.armor.IArmorLogic;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = IArmorLogic.class, remap = false)
public interface MixinIArmorLogic extends IRadiationResistanceArmorLogic {

}
