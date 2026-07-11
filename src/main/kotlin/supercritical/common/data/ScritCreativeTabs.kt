package supercritical.common.data

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.RegistrateDisplayItemsGenerator
import com.tterrag.registrate.util.entry.RegistryEntry
import net.minecraft.world.item.CreativeModeTab
import supercritical.common.registry.ScritRegistration
import supercritical.util.scId

/**
 * Supercritical's single creative tab. GTRegistrate is per-instance isolated (an addon cannot
 * inject into GTCEu's tabs), so — like every GTCEu addon — we own one tab and tag all SC content
 * into it by setting it as the currentTab before any SC entry registers (see Supercritical#init).
 * RegistrateDisplayItemsGenerator iterates THIS registrate's blocks/items, so everything registered
 * while currentTab == SUPERCRITICAL appears here (casings, panelling, plain items, and machines).
 */
object ScritCreativeTabs {
    private val REGISTRATE: GTRegistrate = ScritRegistration.REGISTRATE

    val MAIN: RegistryEntry<CreativeModeTab> = REGISTRATE
        .defaultCreativeTab("main") { builder: CreativeModeTab.Builder ->
            builder
                .displayItems(RegistrateDisplayItemsGenerator("main", REGISTRATE))
                .icon { ScritMachines.FISSION_REACTOR.asStack() }
                .title(REGISTRATE.addLang("itemGroup", scId("main"), "Supercritical"))
                .build()
        }
        .register()

    /** Forces object initialization so the tab entry registers before currentTab is set. */
    fun init() {}
}
