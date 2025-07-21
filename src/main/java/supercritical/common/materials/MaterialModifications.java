package supercritical.common.materials;

import static gregtech.api.unification.material.Materials.*;
import static supercritical.api.unification.material.SCMaterials.HighPressureSteam;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import supercritical.api.unification.material.properties.CoolantProperty;
import supercritical.api.unification.material.properties.FissionFuelProperty;
import supercritical.api.unification.material.properties.ModeratorProperty;
import supercritical.api.unification.material.properties.SCPropertyKey;

public class MaterialModifications {

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
        // Uraninite // TODO: How???
        Uraninite.setProperty(SCPropertyKey.FISSION_FUEL,
                FissionFuelProperty.builder(Uraninite.getRegistryName(), 1800, 60000, 2.4)
                        .fastNeutronCaptureCrossSection(0.5)
                        .slowNeutronCaptureCrossSection(1)
                        .slowNeutronFissionCrossSection(1)
                        .requiredNeutrons(1)
                        .releasedNeutrons(2.5)
                        .releasedHeatEnergy(0.01)
                        .decayRate(0.001)
                        .build());
        // Uranium Hexafluoride
        // UraniumHexafluoride // TODO: How???

        // Depleted Uranium Hexafluoride
        // DepletedUraniumHexafluoride // TODO: How???

        // Uranium Triplatinum
        // UraniumTriplatinum // TODO: How???

        // Uranium Rhodium Dinaquadide
        // UraniumRhodiumDinaquadide // TODO: How???

        // Distilled Water
        DistilledWater.setProperty(SCPropertyKey.COOLANT,
                new CoolantProperty(DistilledWater, HighPressureSteam, FluidStorageKeys.LIQUID, 2., 1000,
                        373, 2260000, 4168.)
                                .setAccumulatesHydrogen(true).setSlowAbsorptionFactor(0.1875)
                                .setFastAbsorptionFactor(0.0625));

        Graphite.setProperty(SCPropertyKey.MODERATOR, ModeratorProperty.builder()
                .maxTemperature(3650)
                .absorptionFactor(0.0625)
                .moderationFactor(3).build());
        Graphite.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK);

        Beryllium.setProperty(SCPropertyKey.MODERATOR, ModeratorProperty.builder()
                .maxTemperature(1500)
                .absorptionFactor(0.015625)
                .moderationFactor(5).build());
        Beryllium.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK);
    }
}
