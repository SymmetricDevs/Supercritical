package gregicality.nuclear.common.materials;

import static gregicality.nuclear.api.unification.material.GCYNMaterials.*;
import static gregicality.nuclear.api.util.GCYNUtility.gcynId;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.DULL;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.unification.material.Material;

public class GCYNUnknownCompositionMaterials {

    public static void register() {
        Corium = new Material.Builder(1560, gcynId("corium"))
                .liquid(new FluidBuilder().temperature(2500).block().density(8.0D).viscosity(10000))
                .color(0x7A6B50)
                .iconSet(DULL)
                .flags(NO_UNIFICATION, STICKY, GLOWING)
                .build();

        SpentUraniumFuelSolution = new Material.Builder(1651, gcynId("spent_uranium_fuel_solution"))
                .liquid()
                .color(0x384536).build();

        RadonRichGasMixture = new Material.Builder(1652, gcynId("radon_rich_gas_mixture"))
                .gas()
                .color(0xd78dd9).build();
    }
}
