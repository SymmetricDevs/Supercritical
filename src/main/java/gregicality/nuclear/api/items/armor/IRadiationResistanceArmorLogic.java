package gregicality.nuclear.api.items.armor;

public interface IRadiationResistanceArmorLogic {

    /**
     * @return the value to multiply radiation damage by
     */
    default float getRadiationResistance() {
        return 1.0f;
    }
}
