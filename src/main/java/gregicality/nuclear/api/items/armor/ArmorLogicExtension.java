package gregicality.nuclear.api.items.armor;

import gregicality.nuclear.mixins.gregtech.MixinIArmorLogic;
import gregicality.nuclear.mixins.gregtech.MixinMetaPrefixItem;
import gregtech.api.items.armor.IArmorLogic;

/**
 * An interface for radiation resistance implementation in {@link IArmorLogic}.
 * Implement this in your armor logic class and override the default value to apply the resistance.
 * Also see {@link MixinIArmorLogic} and {@link MixinMetaPrefixItem}.
 */
public interface ArmorLogicExtension {

    /**
     * @return the value to multiply radiation damage by
     */
    default float getRadiationResistance() {
        return 1.0f;
    }
}
