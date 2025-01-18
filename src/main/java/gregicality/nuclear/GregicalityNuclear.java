package gregicality.nuclear;

import gregicality.GCYNInternalTags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = GCYNInternalTags.MODID, version = GCYNInternalTags.VERSION, name = GCYNInternalTags.MODNAME, acceptedMinecraftVersions = "[1.12.2]")
public class GregicalityNuclear {

    public static final Logger LOGGER = LogManager.getLogger(GCYNInternalTags.MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
