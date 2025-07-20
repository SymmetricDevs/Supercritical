package supercritical.api.unification.material.properties;

import gregtech.api.unification.material.properties.PropertyKey;

public class SCPropertyKey {

    public static final PropertyKey<CoolantProperty> COOLANT = new PropertyKey<>("coolant", CoolantProperty.class);
    public static final PropertyKey<FissionFuelProperty> FISSION_FUEL = new PropertyKey<>("fission_fuel",
            FissionFuelProperty.class);
    public static final PropertyKey<ModeratorProperty> MODERATOR = new PropertyKey<>("moderator", ModeratorProperty.class);
}
