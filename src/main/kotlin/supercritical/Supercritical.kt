package supercritical;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NullMarked;
import supercritical.api.recipes.SCRecipeMaps;
import supercritical.api.registries.SCRegistries;
import supercritical.api.unification.material.SCMaterials;
import supercritical.api.unification.ore.SCOrePrefix;
import supercritical.common.SCConfigHolder;
import supercritical.common.registry.SCBlocks;
import supercritical.common.registry.SCItems;
import supercritical.common.registry.SCMachines;
import supercritical.data.SCDatagen;
import supercritical.loaders.recipe.SCRecipeManager;

@NullMarked
@Mod(BuildConfig.MOD_ID)
public final class Supercritical {

    public static final Logger LOGGER = LogManager.getLogger(BuildConfig.MOD_ID);

    public Supercritical() {
        init();

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SCRegistries.REGISTRATE.registerEventListeners(modBus);
        SCItems.register(modBus);
        SCBlocks.register(modBus);
        modBus.<GTCEuAPI.RegisterEvent<ResourceLocation,GTRecipeType>,GTRecipeType>addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        modBus.<GTCEuAPI.RegisterEvent<ResourceLocation,MachineDefinition>,MachineDefinition>addGenericListener(MachineDefinition.class, this::registerMachines);
        modBus.<FMLCommonSetupEvent>addListener(this::commonSetup);

        modBus.register(SCMaterials.class);
    }

    public static void init() {
        SCConfigHolder.register();
        SCOrePrefix.init();
        SCDatagen.init();
    }

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        SCRecipeMaps.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        SCMachines.ensureInitialized();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(SCRecipeManager::load);
        event.enqueueWork(SCMaterials::registerFuelItems);
        event.enqueueWork(() -> {
            SCMaterials.registerCoolants();
            SCRecipeManager.loadLatest();
        });
        LOGGER.info("{} common setup.", BuildConfig.MOD_NAME);
    }
}
