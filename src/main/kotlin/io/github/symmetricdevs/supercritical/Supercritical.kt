package io.github.symmetricdevs.supercritical

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import io.github.symmetricdevs.supercritical.BuildConfig
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import io.github.symmetricdevs.supercritical.config.ScritConfig
import io.github.symmetricdevs.supercritical.data.ScritDatagen
import io.github.symmetricdevs.supercritical.data.recipe.ScritRecipeManager
import io.github.symmetricdevs.supercritical.util.addGenericListener
import io.github.symmetricdevs.supercritical.common.data.ScritBlocks
import io.github.symmetricdevs.supercritical.common.data.ScritCreativeTabs
import io.github.symmetricdevs.supercritical.common.data.ScritItems
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.common.data.ScritMaterials
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeTypes

typealias GTRecipeEvent = GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType>
typealias GTMachineEvent = GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition>

@Mod(BuildConfig.MOD_ID)
class Supercritical {

    init {
        ScritConfig.init()
        ScritDatagen.init()

        val modBus = FMLJavaModLoadingContext.get().modEventBus
        ScritRegistration.REGISTRATE.registerEventListeners(modBus)
        // Create the Supercritical creative tab and mark it current so all subsequently
        // registered SC blocks/items/machines are tagged into it (GTCEu's GTMachines does the same).
        ScritCreativeTabs.init()
        ScritRegistration.REGISTRATE.creativeModeTab(ScritCreativeTabs.MAIN)
        // Force object initialization -> each `val` registers its entry via GTRegistrate.
        ScritBlocks.init()
        ScritItems.init()
        modBus.addGenericListener<GTRecipeEvent, GTRecipeType> { registerRecipeTypes(it) }
        modBus.addGenericListener<GTMachineEvent, MachineDefinition> { registerMachines(it) }
        modBus.addListener<FMLCommonSetupEvent> { commonSetup(it) }

        modBus.register(ScritMaterials)


    }

    private fun registerRecipeTypes(event: GTRecipeEvent) {
        ScritRecipeTypes.init()
    }

    private fun registerMachines(event: GTMachineEvent) {
        ScritMachines.ensureInitialized()
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        event.enqueueWork { ScritRecipeManager.load() }
        event.enqueueWork { ScritMaterials.registerFuelItems() }
        event.enqueueWork {
            ScritMaterials.registerModerators()
            ScritMaterials.registerCoolants()
            ScritRecipeManager.loadLatest()
        }
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(BuildConfig.MOD_ID)
    }
}
