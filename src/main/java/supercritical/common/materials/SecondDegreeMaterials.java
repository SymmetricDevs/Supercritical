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

        LEU235.setProperty(SCPropertyKey.FISSION_FUEL, new FissionFuelProperty(1500, 750,
                LEU235.getRegistryName(), 3.5)
                .setSlowNeutronCaptureCrossSection(1.5)
                .setSlowNeutronFissionCrossSection(1.5)
                .setDecayRate(200)
                .setReleasedNeutrons(3)
                .setReleasedHeatEnergy(1000)
        );

        HEU235 = new Material.Builder(1002, scId("heu_235"))
                .dust(3)
                .color(0x424845).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 4)
                .build()
                .setFormula("UO2", true);

        HEU235.setProperty(SCPropertyKey.FISSION_FUEL, new FissionFuelProperty(1800, 600,
                HEU235.getRegistryName(), 2.5)
                .setSlowNeutronCaptureCrossSection(2)
                .setSlowNeutronFissionCrossSection(2)
                .setDecayRate(300)
                .setReleasedNeutrons(3)
                .setReleasedHeatEnergy(1000)
        );

        LowGradeMOX = new Material.Builder(1003, scId("low_grade_mox"))
                .dust(3)
                .color(0x62C032).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(FissilePlutoniumDioxide, 1, Uraninite, 19)
                .build()
                .setFormula("(U,Pu)O2", true);

        LowGradeMOX.setProperty(SCPropertyKey.FISSION_FUEL, new FissionFuelProperty(1600, 1000,
                LowGradeMOX.getRegistryName(), 1.5)
                .setSlowNeutronCaptureCrossSection(2)
                .setSlowNeutronFissionCrossSection(2)
                .setDecayRate(200)
                .setReleasedNeutrons(3)
                .setReleasedHeatEnergy(2000)
        );

        HighGradeMOX = new Material.Builder(1004, scId("high_grade_mox"))
                .dust(3)
                .color(0x7EA432).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(FissilePlutoniumDioxide, 1, Uraninite, 4)
                .build()
                .setFormula("(U,Pu)O2", true);

        HighGradeMOX.setProperty(SCPropertyKey.FISSION_FUEL, new FissionFuelProperty(2000, 800,
                HighGradeMOX.getRegistryName(), 1)
                .setSlowNeutronCaptureCrossSection(2.4)
                .setSlowNeutronFissionCrossSection(2.4)
                .setDecayRate(200)
                .setReleasedNeutrons(3)
                .setReleasedHeatEnergy(2000)
        );
    }
}
