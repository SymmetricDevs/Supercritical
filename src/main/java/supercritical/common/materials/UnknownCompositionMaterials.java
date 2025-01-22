package supercritical.common.materials;

import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.DULL;
import static supercritical.api.unification.material.SCMaterials.*;
import static supercritical.api.util.SCUtility.scId;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.unification.material.Material;

/*
 * Ranges 1500-1999
 */
public class UnknownCompositionMaterials {

    public static void register() {
        Corium = new Material.Builder(1501, scId("corium"))
                .liquid(new FluidBuilder().temperature(2500).block().density(8.0D).viscosity(10000))
                .color(0x7A6B50)
                .iconSet(DULL)
                .flags(NO_UNIFICATION, STICKY, GLOWING)
                .build();

        SpentUraniumFuelSolution = new Material.Builder(1502, scId("spent_uranium_fuel_solution"))
                .liquid()
                .color(0x384536).build();

        RadonRichGasMixture = new Material.Builder(1503, scId("radon_rich_gas_mixture"))
                .gas()
                .color(0xd78dd9).build();
    }
}
