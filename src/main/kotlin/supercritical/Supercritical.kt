package supercritical

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jspecify.annotations.NullMarked
import supercritical.api.recipes.SCRecipeMaps
import supercritical.api.registries.SCRegistries
import supercritical.api.unification.material.SCMaterials
import supercritical.api.unification.ore.SCOrePrefix
import supercritical.common.SCConfigHolder
import supercritical.common.registry.SCBlocks
import supercritical.common.registry.SCItems
import supercritical.common.registry.SCMachines
import supercritical.data.SCDatagen
import supercritical.loaders.recipe.SCRecipeManager
import java.util.function.Consumer
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@NullMarked
@Mod(BuildConfig.MOD_ID)
class Supercritical {
    init {
        init()

        val modBus = MOD_BUS
        SCRegistries.REGISTRATE.registerEventListeners(modBus)
        SCItems.register(modBus)
        SCBlocks.register(modBus)
        modBus.addGenericListener(
            GTRecipeType::class.java
        ) { event: GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> -> this.registerRecipeTypes(event) }
        modBus.addGenericListener(
            MachineDefinition::class.java
        ) { event: GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> -> this.registerMachines(event) }
        modBus.addListener { event: FMLCommonSetupEvent -> this.commonSetup(event) }

        modBus.register(SCMaterials::class.java)
    }

    private fun registerRecipeTypes(event: GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType>) {
        SCRecipeMaps.init()
    }

    private fun registerMachines(event: GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition>) {
        SCMachines.ensureInitialized()
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        event.enqueueWork { SCRecipeManager.load() }
        event.enqueueWork { SCMaterials.registerFuelItems() }
        event.enqueueWork {
            SCMaterials.registerCoolants()
            SCRecipeManager.loadLatest()
        }
        LOGGER.info("{} common setup.", BuildConfig.MOD_NAME)
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(BuildConfig.MOD_ID)

        fun init() {
            SCConfigHolder.register()
            SCOrePrefix.init()
            SCDatagen.init()
        }
    }
}
