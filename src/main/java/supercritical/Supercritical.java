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

@Mod(modid = Tags.MOD_ID, version = Tags.MOD_VERSION, name = Tags.MOD_NAME, dependencies = "required-after:gregtech@[2.8.10-beta,);"
		+ "required-after:mixinbooter@[9.0,);", acceptedMinecraftVersions = "[1.12.2]")
public class Supercritical {

	@SidedProxy(modId = Tags.MOD_ID, clientSide = "supercritical.client.ClientProxy", serverSide = "supercritical.common.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Instance(Tags.MOD_ID)
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
		proxy.postLoad();
	}
}
