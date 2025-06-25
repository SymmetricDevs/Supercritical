package supercritical.api.unification.material;

import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.DULL;
import static supercritical.api.util.SCUtility.scId;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.unification.material.Material;
import supercritical.common.SCConfigHolder;
import supercritical.common.materials.ElementMaterials;
import supercritical.common.materials.FirstDegreeMaterials;
import supercritical.common.materials.SecondDegreeMaterials;
import supercritical.common.materials.UnknownCompositionMaterials;

public class SCMaterials {

    /* public static Material Neptunium = Materials.Neptunium; */
    public static Material Neptunium235;
    public static Material Neptunium236;
    public static Material Neptunium237;
    public static Material Neptunium239;

    public static Material Plutonium; // Overrides CEu's Plutonium
    public static Material Plutonium238;
    /* public static Material Plutonium239 = Materials.Plutonium239; */
    public static Material Plutonium240;
    /* public static Material Plutonium241 = Materials.Plutonium241; */
    public static Material Plutonium242;
    public static Material Plutonium244;

    public static Material Uranium; // Overrides CEu's Uranium
    /* public static Material Uranium235 = Materials.Uranium235; */
    /* public static Material Uranium238 = Materials.Uranium238; */
    public static Material Uranium239;

    public static Material HighEnrichedUraniumHexafluoride;
    /* public static Material LowEnrichedUraniumHexafluoride = Materials.EnrichedUraniumHexafluoride; */

    public static Material LowEnrichedUraniumDioxide;

    public static Material HighEnrichedUraniumDioxide;
    public static Material DepletedUraniumDioxide;
    public static Material HighPressureSteam;
    public static Material FissilePlutoniumDioxide;
    public static Material Zircaloy;
    public static Material Zircon;
    public static Material ZirconiumDioxide;
    public static Material ZirconiumTetrachloride;
    public static Material HafniumDioxide;
    public static Material HafniumTetrachloride;
    public static Material Inconel;
    public static Material BoronTrioxide;
    public static Material BoronCarbide;
    public static Material HeavyWater;
    public static Material HighPressureHeavyWater;

    public static Material Corium;
    public static Material SpentUraniumFuelSolution;
    public static Material RadonRichGasMixture;

    public static Material LEU235;
    public static Material HEU235;
    public static Material LowGradeMOX;
    public static Material HighGradeMOX;

    public static void register() {
        /*
         * Registers Curium with id 0.
         * Essential for reactor meltdown.
         */
        Corium = new Material.Builder(0, scId("corium"))
                .liquid(new FluidBuilder()
                        .temperature(2500)
                        .density(8.0D)
                        .viscosity(10000))
                .color(0x7A6B50)
                .iconSet(DULL)
                .flags(NO_UNIFICATION, STICKY, GLOWING)
                .build();

        if (SCConfigHolder.misc.disableAllMaterials) return;

        /*
         * Ranges 1-499
         */
        ElementMaterials.register();

        /*
         * Ranges 500-999
         */
        FirstDegreeMaterials.register();

        /*
         * Ranges 1000-1499
         */
        SecondDegreeMaterials.register();

        /*
         * Ranges 1500-1999
         */
        UnknownCompositionMaterials.register();
    }
}
