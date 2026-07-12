package io.github.symmetricdevs.supercritical.integration.opencomputers

import li.cil.oc.api.Driver
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import io.github.symmetricdevs.supercritical.BuildConfig
import io.github.symmetricdevs.supercritical.Supercritical
import io.github.symmetricdevs.supercritical.integration.opencomputers.drivers.specific.DriverFissionReactor

/**
 * Self-registering hook for the OpenComputers Community Edition (OC:CE) integration. Mirrors
 * [io.github.symmetricdevs.supercritical.integration.computercraft.ComputerCraftIntegration]: a standalone
 * `@EventBusSubscriber` so the loading-context code (`Supercritical.kt`) never references
 * OpenComputers. Forge's `AutomaticEventSubscriber` discovers this annotation on the classpath, so
 * no explicit wiring in `Supercritical.kt` is required.
 *
 * IMPORTANT: Forge's `@EventBusSubscriber` only registers **static** `@SubscribeEvent` methods
 * (it scans the class, not instances). A Kotlin `object` instance method is silently ignored,
 * hence `@JvmStatic` here so `commonSetup` is emitted as a static method on this class.
 *
 * The driver is registered during `FMLCommonSetupEvent` via `li.cil.oc.api.Driver.add(...)` —
 * the same registration entry point the 1.12.2 integration used (GTCEu 1.12.2's
 * `OpenComputersModule.registerDriver` was itself a `Driver.add` wrapper). The call is guarded by
 * an `opencomputers` mod-loaded check so the integration is a no-op when OC:CE is absent. OC:CE is
 * a `modCompileOnlyApi` + `modRuntimeOnly` dependency, so the OC API classes are only resolvable
 * when OC:CE is present; the `Driver` / `DriverFissionReactor` references live solely inside the
 * guarded branch and are never class-loaded when OC:CE is missing (JVM lazy resolution), matching
 * the CC:T integration's optional-mod pattern.
 *
 * `Driver.add` appends to OC's driver list during mod loading; OC's Adapter block consults that
 * list lazily at runtime, so calling it synchronously from the setup worker thread is safe (the
 * same reasoning as the CC:T integration's synchronous `registerPeripheralProvider`).
 */
@EventBusSubscriber(modid = BuildConfig.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
object OpenComputersIntegration {

    @SubscribeEvent
    @JvmStatic
    fun commonSetup(event: FMLCommonSetupEvent) {
        val ocLoaded = ModList.get().isLoaded(OPENCOMPUTERS_MOD_ID)
        Supercritical.LOGGER.info("OpenComputers integration setup (opencomputers loaded={})", ocLoaded)
        if (ocLoaded) {
            Supercritical.LOGGER.info("Registering fission reactor OpenComputers driver...")
            Driver.add(DriverFissionReactor())
        }
    }

    private const val OPENCOMPUTERS_MOD_ID = "opencomputers"
}
