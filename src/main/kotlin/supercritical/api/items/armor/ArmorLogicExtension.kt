package supercritical.api.items.armor;

/**
 * Extends {@link com.gregtechceu.gtceu.api.item.armor.IArmorLogic} with radiation resistance.
 */
public interface ArmorLogicExtension {

    /**
     * @return the value to multiply radiation damage by
     */
    default float getRadiationResistance() {
        return 1.0f;
    }

    static float getRadiationResistance(com.gregtechceu.gtceu.api.item.armor.IArmorLogic armorLogic) {
        return ((ArmorLogicExtension) armorLogic).getRadiationResistance();
    }
}
