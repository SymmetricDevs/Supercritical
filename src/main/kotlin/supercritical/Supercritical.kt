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
import supercritical.common.data.ScritMaterials
import supercritical.common.data.ScritRecipeTypes
import supercritical.common.registry.ScritBlocks
import supercritical.common.data.ScritItems
import supercritical.common.data.ScritMachines
import supercritical.common.registry.ScritRegistration
import supercritical.config.ScritConfig
import supercritical.data.ScritDatagen
import supercritical.data.recipe.ScritRecipeManager
import supercritical.util.addGenericListener

typealias GTRecipeEvent = GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType>
typealias GTMachineEvent = GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition>

@Mod(BuildConfig.MOD_ID)
class Supercritical {

    init {
        ScritConfig.init()
        ScritDatagen.init()

        val modBus = FMLJavaModLoadingContext.get().modEventBus
        ScritRegistration.REGISTRATE.registerEventListeners(modBus)
        ScritItems.register(modBus)
        ScritBlocks.register(modBus)
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
        LOGGER.info("{} common setup.", BuildConfig.MOD_NAME)
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(BuildConfig.MOD_ID)
    }
}
