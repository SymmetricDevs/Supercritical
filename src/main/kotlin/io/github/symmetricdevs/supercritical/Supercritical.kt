package io.github.symmetricdevs.supercritical

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import io.github.symmetricdevs.supercritical.common.data.*
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import io.github.symmetricdevs.supercritical.config.ScritConfig
import io.github.symmetricdevs.supercritical.data.ScritDatagen
import io.github.symmetricdevs.supercritical.util.addGenericListener
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

typealias GTRecipeEvent = GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType>
typealias GTMachineEvent = GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition>

@Mod(BuildConfig.MOD_ID)
class Supercritical {

    init {
        ScritConfig.init()
        ScritDatagen.init()

        @Suppress("removal") val modBus = FMLJavaModLoadingContext.get().modEventBus
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
        ScritMachines.init()
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        // Fission-fuel items, moderators and coolants are registered at runtime here, after their
        // material properties have been attached during GTCEu's MaterialEvent / PostMaterialEvent.
        // Recipes themselves are now generated through datagen (ScritRecipes via IGTAddon.addRecipes),
        // so there is no longer any runtime recipe loading here.
        event.enqueueWork { ScritMaterials.registerFuelItems() }
        event.enqueueWork {
            ScritMaterials.registerModerators()
            ScritMaterials.registerCoolants()
        }
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(BuildConfig.MOD_ID)

        /** Convenience factory for Supercritical resource locations. */
        fun id(path: String): ResourceLocation = ResourceLocation(BuildConfig.MOD_ID, path)
    }
}
