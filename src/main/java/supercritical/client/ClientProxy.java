package supercritical.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import dev.tianmi.sussypatches.common.SusConfig;
import gregtech.api.util.Mods;
import supercritical.client.renderer.textures.SCConnectedTextures;
import supercritical.client.renderer.textures.SCTextures;
import supercritical.common.CommonProxy;
import supercritical.common.blocks.SCMetaBlocks;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        SCMetaBlocks.registerItemModels();
    }

    @Override
    public void preLoad() {
        super.preLoad();
        SCTextures.preInit();
    }

    @Override
    public void postLoad() {
        super.postLoad();
        if (Loader.isModLoaded("sussypatches") && Mods.CTM.isModLoaded() && SusConfig.FEAT.multiCTM) {
            SCConnectedTextures.init();
        }
    }
}
