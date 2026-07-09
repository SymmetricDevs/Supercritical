package supercritical.api.unification.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

public final class SCPropertyKey {

    public static final PropertyKey<CoolantProperty> COOLANT = new PropertyKey<>("coolant", CoolantProperty.class);
    public static final PropertyKey<FissionFuelProperty> FISSION_FUEL = new PropertyKey<>("fission_fuel", FissionFuelProperty.class);
    public static final PropertyKey<ModeratorProperty> MODERATOR = new PropertyKey<>("moderator", ModeratorProperty.class);

    private SCPropertyKey() {}
}
