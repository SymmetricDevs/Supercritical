package supercritical.client.renderer.textures;

import static dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures.*;
import static dev.tianmi.sussypatches.client.renderer.textures.cube.VisualStateRenderer.from;
import static supercritical.api.util.SCUtility.scId;
import static supercritical.common.blocks.BlockFissionCasing.FissionCasingType.REACTOR_VESSEL;
import static supercritical.common.blocks.SCMetaBlocks.*;

import dev.tianmi.sussypatches.client.renderer.textures.cube.VisualStateRenderer;

public class SCConnectedTextures {

    public static final VisualStateRenderer REACTOR_VESSEL_CTM = from(FISSION_CASING.getState(REACTOR_VESSEL));

    public static void init() {
        SOLID_STEEL_CASING_CTM.override(scId("heat_exchanger"));
        REACTOR_VESSEL_CTM.override(scId("fission_reactor"));
        CLEAN_STAINLESS_STEEL_CASING_CTM.override(scId("spent_fuel_pool"));
        INERT_PTFE_CASING_CTM.override(scId("gas_centrifuge"));
    }
}
