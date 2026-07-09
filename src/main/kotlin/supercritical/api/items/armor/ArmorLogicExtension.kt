package supercritical.api.items.armor

import com.gregtechceu.gtceu.api.item.armor.IArmorLogic

/**
 * Extends [IArmorLogic] with radiation resistance.
 */
interface ArmorLogicExtension {
    val radiationResistance: Float
        /**
         * @return the value to multiply radiation damage by
         */
        get() = 1.0f

    companion object {
        fun getRadiationResistance(armorLogic: IArmorLogic): Float {
            return (armorLogic as ArmorLogicExtension).radiationResistance
        }
    }
}
