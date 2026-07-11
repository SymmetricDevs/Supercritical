package supercritical

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import supercritical.api.recipes.SCRecipeMaps
import supercritical.api.registries.SCRegistries
import supercritical.api.unification.material.SCMaterials
import supercritical.api.util.addGenericListener
import supercritical.common.SCConfigHolder
import supercritical.common.registry.SCBlocks
import supercritical.common.registry.SCItems
import supercritical.common.registry.SCMachines
import supercritical.data.SCDatagen
import supercritical.loaders.recipe.SCRecipeManager

typealias GTRecipeEvent = GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType>
typealias GTMachineEvent = GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition>

@Mod(BuildConfig.MOD_ID)
class Supercritical {

    init {
        init()

        val modBus = FMLJavaModLoadingContext.get().modEventBus
        SCRegistries.REGISTRATE.registerEventListeners(modBus)
        SCItems.register(modBus)
        SCBlocks.register(modBus)
        modBus.addGenericListener<GTRecipeEvent, GTRecipeType> { registerRecipeTypes(it) }
        modBus.addGenericListener<GTMachineEvent, MachineDefinition> { registerMachines(it) }
        modBus.addListener<FMLCommonSetupEvent> { commonSetup(it) }

        modBus.register(SCMaterials)
    }

    private fun registerRecipeTypes(event: GTRecipeEvent) {
        SCRecipeMaps.init()
    }

    private fun registerMachines(event: GTMachineEvent) {
        SCMachines.ensureInitialized()
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        event.enqueueWork { SCRecipeManager.load() }
        event.enqueueWork { SCMaterials.registerFuelItems() }
        event.enqueueWork {
            SCMaterials.registerModerators()
            SCMaterials.registerCoolants()
            SCRecipeManager.loadLatest()
        }
        LOGGER.info("{} common setup.", BuildConfig.MOD_NAME)
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(BuildConfig.MOD_ID)

        fun init() {
            SCConfigHolder.init()
            SCDatagen.init()
        }
    }
}
