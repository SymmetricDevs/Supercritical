package supercritical.common.materials;

import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.DULL;
import static supercritical.api.unification.material.SCMaterials.*;
import static supercritical.api.util.SCUtility.scId;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.unification.material.Material;

/*
 * Ranges 10001-10999
 */
public class CoriumMaterials {

    public static void register() {
        Corium = new Material.Builder(10001, scId("corium"))
                .liquid(new FluidBuilder().temperature(2500).block().density(8.0D).viscosity(10000))
                .color(0x7A6B50)
                .iconSet(DULL)
                .flags(NO_UNIFICATION, STICKY, GLOWING)
                .build();
    }
}
