package supercritical.data.forge;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import supercritical.BuildConfig;

@Mod.EventBusSubscriber(modid = BuildConfig.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {

    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        // Registrate-backed providers are registered in SCDatagen.
        // Add non-Registrate providers here when Supercritical needs them.
    }
}
