package supercritical.common.materials;

import static gregtech.api.unification.material.Materials.Uraninite;
import static gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION;
import static gregtech.api.unification.material.info.MaterialIconSet.METALLIC;
import static supercritical.api.unification.material.SCMaterials.*;
import static supercritical.api.util.SCUtility.scId;

import gregtech.api.unification.material.Material;
import supercritical.api.unification.material.properties.FissionFuelProperty;
import supercritical.api.unification.material.properties.SCPropertyKey;

/*
 * Ranges 1000-1499
 */
public class SecondDegreeMaterials {

    public static void register() {
        LEU235 = new Material.Builder(1001, scId("leu_235"))
                .dust(3)
                .color(0x232323).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 19)
                .build()
                .setFormula("UO2", true);

        LEU235.setProperty(SCPropertyKey.FISSION_FUEL,
                FissionFuelProperty.builder(LEU235.getRegistryName(), 1500, 75000, 3.5)
                        .fastNeutronCaptureCrossSection(0.4)
                        .slowNeutronCaptureCrossSection(1.8)
                        .slowNeutronFissionCrossSection(1.8)
                        .requiredNeutrons(1)
                        .releasedNeutrons(2.5)
                        .releasedHeatEnergy(0.025)
                        .decayRate(0.025)
                        .build());

        HEU235 = new Material.Builder(1002, scId("heu_235"))
                .dust(3)
                .color(0x424845).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 4)
                .build()
                .setFormula("UO2", true);

        HEU235.setProperty(SCPropertyKey.FISSION_FUEL,
                FissionFuelProperty.builder(HEU235.getRegistryName(), 1800, 60000, 2.5)
                        .fastNeutronCaptureCrossSection(0.3)
                        .slowNeutronCaptureCrossSection(2)
                        .slowNeutronFissionCrossSection(2)
                        .requiredNeutrons(1)
                        .releasedNeutrons(2.5)
                        .releasedHeatEnergy(0.025)
                        .decayRate(0.05)
                        .build());

        LowGradeMOX = new Material.Builder(1003, scId("low_grade_mox"))
                .dust(3)
                .color(0x62C032).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(FissilePlutoniumDioxide, 1, Uraninite, 19)
                .build()
                .setFormula("(U,Pu)O2", true);

        LowGradeMOX.setProperty(SCPropertyKey.FISSION_FUEL,
                FissionFuelProperty.builder(LowGradeMOX.getRegistryName(), 1600, 50000, 1.5)
                        .fastNeutronCaptureCrossSection(0.5)
                        .slowNeutronCaptureCrossSection(2.2)
                        .slowNeutronFissionCrossSection(2.2)
                        .requiredNeutrons(1)
                        .releasedNeutrons(2.60)
                        .releasedHeatEnergy(0.052)
                        .decayRate(0.1)
                        .build());

        HighGradeMOX = new Material.Builder(1004, scId("high_grade_mox"))
                .dust(3)
                .color(0x7EA432).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(FissilePlutoniumDioxide, 1, Uraninite, 4)
                .build()
                .setFormula("(U,Pu)O2", true);

        HighGradeMOX.setProperty(SCPropertyKey.FISSION_FUEL,
                FissionFuelProperty.builder(HighGradeMOX.getRegistryName(), 2000, 80000, 1)
                        .fastNeutronCaptureCrossSection(0.5)
                        .slowNeutronCaptureCrossSection(2.4)
                        .slowNeutronFissionCrossSection(2.4)
                        .requiredNeutrons(1)
                        .releasedNeutrons(2.80)
                        .releasedHeatEnergy(0.056)
                        .decayRate(0.2)
                        .build());
    }
}
