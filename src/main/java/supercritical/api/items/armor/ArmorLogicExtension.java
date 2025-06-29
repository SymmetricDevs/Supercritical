package supercritical.api.items.armor;

import gregtech.api.items.armor.IArmorLogic;
import lombok.experimental.UtilityClass;

/// An interface for radiation resistance implementation in [IArmorLogic].
/// Implement this in your armor logic class and override the default value to apply the resistance.
/// Also see [MixinIArmorLogic] and [MixinMetaPrefixItem].
public interface ArmorLogicExtension {

    /**
     * @return the value to multiply radiation damage by
     */
    default float getRadiationResistance() {
        return 1.0f;
    }

    @UtilityClass
    class Handler {

        public float getRadiationResistance(IArmorLogic armorLogic) {
            return ((ArmorLogicExtension) armorLogic).getRadiationResistance();
        }
    }
}
