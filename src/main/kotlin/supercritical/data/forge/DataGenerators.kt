package supercritical.data.forge

import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import supercritical.BuildConfig

@EventBusSubscriber(modid = BuildConfig.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
object DataGenerators {
    @SubscribeEvent
    fun gatherData(event: GatherDataEvent?) {
        // Registrate-backed providers are registered in SCDatagen.
        // Add non-Registrate providers here when Supercritical needs them.
    }
}
