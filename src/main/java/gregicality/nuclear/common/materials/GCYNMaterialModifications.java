package gregicality.nuclear.common.materials;

import static gregicality.nuclear.api.unification.material.GCYNMaterials.HighPressureSteam;
import static gregtech.api.unification.material.Materials.*;

import gregicality.nuclear.api.unification.material.properties.CoolantProperty;
import gregicality.nuclear.api.unification.material.properties.FissionFuelProperty;
import gregicality.nuclear.api.unification.material.properties.GCYNPropertyKey;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;

public class GCYNMaterialModifications {

    public static void init() {
        // Zirconium
        Zirconium.setProperty(PropertyKey.DUST, new DustProperty());

        // Hafnium
        Hafnium.addFlags(MaterialFlags.GENERATE_LONG_ROD);
        Hafnium.setProperty(PropertyKey.BLAST, new BlastProperty(2227));
        // Hafnium.setProperty(PropertyKey.INGOT, new IngotProperty());

        // Plutonium-239
        Plutonium239.getProperty(PropertyKey.ORE).setOreByProducts();

        // Uranium-238
        Uranium238.setMaterialRGB(0x46FA46);
        Uranium238.setMaterialIconSet(MaterialIconSet.SHINY);

        // Salt
        Salt.setProperty(PropertyKey.FLUID,
                new FluidProperty(FluidStorageKeys.LIQUID, new FluidBuilder().translation("gregtech.fluid.molten")));

        // Stainless Steel
        StainlessSteel.addFlags(MaterialFlags.GENERATE_ROUND);

        // Uraninite
        // Uraninite. // TODO: How???
        Uraninite.setProperty(GCYNPropertyKey.FISSION_FUEL, new FissionFuelProperty(
                1800, 300, 55., 1.,
                1000., 0., 2.4, Uraninite.getRegistryName()));

        // Uranium Hexafluoride
        // UraniumHexafluoride // TODO: How???

        // Depleted Uranium Hexafluoride
        // DepletedUraniumHexafluoride // TODO: How???

        // Uranium Triplatinum
        // UraniumTriplatinum // TODO: How???

        // Uranium Rhodium Dinaquadide
        // UraniumRhodiumDinaquadide // TODO: How???

        // Distilled Water
        DistilledWater.setProperty(GCYNPropertyKey.COOLANT,
                new CoolantProperty(DistilledWater, HighPressureSteam, FluidStorageKeys.LIQUID, 1., 1000,
                        373, 2260000, 4168.)
                                .setAccumulatesHydrogen(true));
    }
}
