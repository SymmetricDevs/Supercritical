package supercritical.common;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import supercritical.client.renderer.textures.SCTextures;
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
}
