package supercritical.common.materials;

import static gregtech.api.unification.material.Materials.Uraninite;
import static gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION;
import static gregtech.api.unification.material.info.MaterialIconSet.METALLIC;
import static supercritical.api.unification.material.SCMaterials.*;
import static supercritical.api.util.SCUtility.scId;

import gregtech.api.unification.material.Material;
import supercritical.api.unification.material.properties.FissionFuelProperty;
import supercritical.api.unification.material.properties.SCPropertyKey;

public class SecondDegreeMaterials {

    public static void register() {
        LEU235 = new Material.Builder(2065, scId("leu_235"))
                .dust(3)
                .color(0x232323).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 19)
                .build()
                .setFormula("UO2", true);

        LEU235.setProperty(SCPropertyKey.FISSION_FUEL, new FissionFuelProperty(
                1500, 750, 55., 1.,
                2500., 0., 3.5, LEU235.getRegistryName()));

        HEU235 = new Material.Builder(2066, scId("heu_235"))
                .dust(3)
                .color(0x424845).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 4)
                .build()
                .setFormula("UO2", true);

        HEU235.setProperty(SCPropertyKey.FISSION_FUEL, new FissionFuelProperty(
                1800, 600, 40., 1.,
                3000., 0., 2.5, HEU235.getRegistryName()));

        LowGradeMOX = new Material.Builder(2067, scId("low_grade_mox"))
                .dust(3)
                .color(0x62C032).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(FissilePlutoniumDioxide, 1, Uraninite, 19)
                .build()
                .setFormula("(U,Pu)O2", true);

        LowGradeMOX.setProperty(SCPropertyKey.FISSION_FUEL, new FissionFuelProperty(
                1600, 1000, 50., 10.,
                4000., 10., 1.5, LowGradeMOX.getRegistryName()));

        HighGradeMOX = new Material.Builder(2068, scId("high_grade_mox"))
                .dust(3)
                .color(0x7EA432).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(FissilePlutoniumDioxide, 1, Uraninite, 4)
                .build()
                .setFormula("(U,Pu)O2", true);

        HighGradeMOX.setProperty(SCPropertyKey.FISSION_FUEL, new FissionFuelProperty(
                2000, 800, 35., 25.,
                5500., 0., 1, HighGradeMOX.getRegistryName()));
    }
}
