package io.github.symmetricdevs.supercritical.integration.computercraft

import dan200.computercraft.api.ForgeComputerCraftAPI
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import io.github.symmetricdevs.supercritical.BuildConfig
import io.github.symmetricdevs.supercritical.Supercritical

/**
 * Self-registering hook for the CC:Tweaked integration. This is a standalone `@EventBusSubscriber`
 * (mirroring [io.github.symmetricdevs.supercritical.data.forge.DataGenerators]' annotation) so the loading-context code
 * (`Supercritical.kt` / `SCRegistries.kt`) never references ComputerCraft.
 *
 * IMPORTANT: Forge's `@EventBusSubscriber` only registers **static** `@SubscribeEvent` methods
 * (it scans the class, not instances — GTCEu's `ToolEventHandlers`/`ForgeCommonEventListener`
 * follow the same `public static void` rule). A Kotlin `object` instance method is silently
 * ignored, hence `@JvmStatic` here so `commonSetup` is emitted as a static method on this class.
 *
 * The provider is registered during `FMLCommonSetupEvent` (the same point GTCEu registers its CC:T
 * sources), guarded by a `computercraft` mod-loaded check so the integration is a no-op when CC:T
 * is absent. CC:T is a `compileOnly` + `modRuntimeOnly` dependency, so this class only loads when
 * CC:T is on the classpath.
 */
@EventBusSubscriber(modid = BuildConfig.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
object ComputerCraftIntegration {

    @SubscribeEvent
    @JvmStatic
    fun commonSetup(event: FMLCommonSetupEvent) {
        // Synchronous (not deferred via enqueueWork) so the log line and the provider registration
        // are guaranteed to take effect the instant this handler runs. registerPeripheralProvider
        // only appends to ComputerCraft's provider list during mod loading; CC:T consults it lazily
        // at runtime, so this is thread-safe to call from the setup worker thread.
        val ccLoaded = ModList.get().isLoaded(COMPUTERCRAFT_MOD_ID)
        Supercritical.LOGGER.info("ComputerCraft integration setup (computercraft loaded={})", ccLoaded)
        if (ccLoaded) {
            Supercritical.LOGGER.info("Registering fission reactor CC:T peripheral provider...")
            ForgeComputerCraftAPI.registerPeripheralProvider(FissionReactorPeripheralProvider)
        }
    }

    private const val COMPUTERCRAFT_MOD_ID = "computercraft"
}
