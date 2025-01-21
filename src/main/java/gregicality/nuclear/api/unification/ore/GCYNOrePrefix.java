package gregicality.nuclear.api.unification.ore;

import gregicality.nuclear.api.unification.material.info.GCYNMaterialIconType;
import gregicality.nuclear.api.unification.material.properties.GCYNPropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.client.resources.I18n;

import java.util.Collections;
import java.util.function.Function;

public class GCYNOrePrefix {
    // Nuclear stuff, introduced by Zalgo and Bruberu
    public static final OrePrefix fuelRod = new OrePrefix("fuelRod", -1, null, GCYNMaterialIconType.fuelRod, 0,
            material -> material.hasProperty(GCYNPropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelRodDepleted = new OrePrefix("fuelRodDepleted", -1, null,
            GCYNMaterialIconType.fuelRodDepleted, 0, material -> material.hasProperty(GCYNPropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelRodHotDepleted = new OrePrefix("fuelRodHotDepleted", -1, null,
            GCYNMaterialIconType.fuelRodHotDepleted, 0, material -> material.hasProperty(GCYNPropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelPellet = new OrePrefix("fuelPellet", -1, null,
            GCYNMaterialIconType.fuelPellet, 0, material -> material.hasProperty(GCYNPropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelPelletDepleted = new OrePrefix("fuelPelletDepleted", -1, null,
            GCYNMaterialIconType.fuelPelletDepleted, 0, material -> material.hasProperty(GCYNPropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));

    public static final OrePrefix dustSpentFuel = new OrePrefix("dustSpentFuel", -1, null,
            GCYNMaterialIconType.dustSpentFuel, 0, material -> material.hasProperty(GCYNPropertyKey.FISSION_FUEL));
    public static final OrePrefix dustBredFuel = new OrePrefix("dustBredFuel", -1, null,
            GCYNMaterialIconType.dustBredFuel, 0, material -> material.hasProperty(GCYNPropertyKey.FISSION_FUEL));
    public static final OrePrefix dustFissionByproduct = new OrePrefix("dustFissionByproduct", -1, null,
            GCYNMaterialIconType.dustFissionByproduct, 0, material -> material.hasProperty(GCYNPropertyKey.FISSION_FUEL));

    public static void init() {
        setRadiationDamageFunction(fuelRod, neutrons -> neutrons / 10e23);
        setRadiationDamageFunction(fuelPellet, neutrons -> neutrons / 160e23);

        setRadiationDamageFunction(fuelRodDepleted, neutrons -> neutrons / 1.5e23);
        setRadiationDamageFunction(fuelRodHotDepleted, neutrons -> neutrons / 1e23);
        fuelRodHotDepleted.heatDamageFunction = x -> 2f;
        setRadiationDamageFunction(fuelPelletDepleted, neutrons -> neutrons / 24e23);
    }

    private static void setRadiationDamageFunction(OrePrefix prefix, Function<Double, Double> function) {
        ((OrePrefixExtension) prefix).setDamageFunction(function);
    }
}
