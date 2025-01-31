package supercritical;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.jetbrains.annotations.NotNull;

import supercritical.common.CommonProxy;
import supercritical.common.blocks.SCMetaBlocks;
import supercritical.common.item.SCMetaItems;
import supercritical.common.metatileentities.SCMetaTileEntities;

@Mod(modid = SCInternalTags.MODID,
     version = SCInternalTags.VERSION,
     name = SCInternalTags.MODNAME,
     dependencies = "required-after:gregtech@[2.8.10-beta,);" +
             "required-after:mixinbooter@[9.0,);",
     acceptedMinecraftVersions = "[1.12.2]")
public class Supercritical {

    @SidedProxy(modId = SCValues.MODID,
                clientSide = "supercritical.common.ClientProxy",
                serverSide = "supercritical.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(SCValues.MODID)
    public static Supercritical instance;

    @Mod.EventHandler
    public void onPreInit(@NotNull FMLPreInitializationEvent event) {
        SCMetaItems.initMetaItems();
        SCMetaBlocks.init();
        SCMetaTileEntities.init();

        proxy.preLoad();
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.onPostLoad();
    }
}
