package supercritical.client.renderer.textures;

import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleCubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;

public class SCTextures {

    public static OrientedOverlayRenderer FISSION_REACTOR_OVERLAY;
    public static OrientedOverlayRenderer SPENT_FUEL_POOL_OVERLAY;
    public static OrientedOverlayRenderer GAS_CENTRIFUGE_OVERLAY;
    public static OrientedOverlayRenderer HEAT_EXCHANGER_OVERLAY;

    public static SimpleOverlayRenderer CONTROL_ROD;
    public static SimpleOverlayRenderer CONTROL_ROD_MODERATED;
    public static SimpleOverlayRenderer MODERATOR_PORT;

    public static SimpleCubeRenderer REACTOR_VESSEL;
    public static SimpleCubeRenderer PTFE_PIPE;

    public static void preInit() {
        FISSION_REACTOR_OVERLAY = new OrientedOverlayRenderer("multiblock/fission_reactor");
        SPENT_FUEL_POOL_OVERLAY = new OrientedOverlayRenderer("multiblock/spent_fuel_pool");
        GAS_CENTRIFUGE_OVERLAY = new OrientedOverlayRenderer("multiblock/gas_centrifuge");
        HEAT_EXCHANGER_OVERLAY = new OrientedOverlayRenderer("multiblock/heat_exchanger");

        CONTROL_ROD = new SimpleOverlayRenderer("overlay/machine/overlay_control_rod");
        CONTROL_ROD_MODERATED = new SimpleOverlayRenderer("overlay/machine/overlay_control_rod_moderated");
        MODERATOR_PORT = new SimpleOverlayRenderer("overlay/machine/overlay_moderator_port");

        REACTOR_VESSEL = new SimpleCubeRenderer("gregtech:blocks/casings/fission/reactor_vessel");
        PTFE_PIPE = new SimpleCubeRenderer(
                "gregtech:blocks/casings/pipe/machine_casing_pipe_polytetrafluoroethylene.png");
    }
}
