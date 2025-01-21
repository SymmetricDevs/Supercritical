package gregicality.nuclear.common;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import gregicality.nuclear.client.renderer.textures.GCYNTextures;
import gregicality.nuclear.common.blocks.GCYNMetaBlocks;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        GCYNMetaBlocks.registerItemModels();
    }

    @Override
    public void preLoad() {
        super.preLoad();
        GCYNTextures.preInit();
    }
}
