package io.github.symmetricdevs.supercritical.data.forge

import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import io.github.symmetricdevs.supercritical.BuildConfig

@EventBusSubscriber(modid = BuildConfig.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
object DataGenerators {
    @SubscribeEvent
    fun gatherData(event: GatherDataEvent) {
        // Blockstates, block/item models, and English lang for SC blocks/items/machines are
        // generated automatically by GTRegistrate's data gen (ScritRegistration.REGISTRATE).
        // Tag/loot providers are registered in ScritDatagen. Add other non-Registrate providers
        // here when Supercritical needs them.
    }
}
