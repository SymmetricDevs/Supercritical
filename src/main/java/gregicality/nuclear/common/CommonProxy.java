package gregicality.nuclear.common;

import gregicality.nuclear.GCYNValues;
import gregicality.nuclear.api.nuclear.fission.CoolantRegistry;
import gregicality.nuclear.api.nuclear.fission.FissionFuelRegistry;
import gregicality.nuclear.api.unification.material.properties.CoolantProperty;
import gregicality.nuclear.api.unification.material.properties.FissionFuelProperty;
import gregicality.nuclear.api.unification.material.properties.GCYNPropertyKey;
import gregicality.nuclear.api.unification.ore.GCYNOrePrefix;
import gregicality.nuclear.api.util.GCYNLog;
import gregicality.nuclear.common.blocks.GCYNMetaBlocks;
import gregicality.nuclear.common.item.GCYNMetaItems;
import gregicality.nuclear.loaders.recipe.GCYNRecipeManager;
import gregicality.nuclear.modules.GCYNModules;
import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.modules.ModuleContainerRegistryEvent;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.event.PostMaterialEvent;
import gregtech.common.items.MetaItems;
import gregtech.modules.ModuleManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;


@Mod.EventBusSubscriber(modid = GCYNValues.MODID)
public class CommonProxy {

    @SubscribeEvent
    public static void syncConfigValues(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(GCYNValues.MODID)) {
            ConfigManager.sync(GCYNValues.MODID, Config.Type.INSTANCE);
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        GCYNLog.logger.info("Registering blocks...");
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(GCYNMetaBlocks.FISSION_CASING);
        registry.register(GCYNMetaBlocks.NUCLEAR_CASING);
        registry.register(GCYNMetaBlocks.GAS_CENTRIFUGE_CASING);
        registry.register(GCYNMetaBlocks.PANELLING);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        GCYNLog.logger.info("Registering Items...");

        GCYNMetaItems.initSubitems();
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(createItemBlock(GCYNMetaBlocks.FISSION_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(GCYNMetaBlocks.NUCLEAR_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(GCYNMetaBlocks.GAS_CENTRIFUGE_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(GCYNMetaBlocks.PANELLING, VariantItemBlock::new));
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        return itemBlock;
    }

    @SubscribeEvent
    public static void postRegisterMaterials(@NotNull PostMaterialEvent event) {
        MetaItems.addOrePrefix(
                GCYNOrePrefix.fuelRod,
                GCYNOrePrefix.fuelRodDepleted,
                GCYNOrePrefix.fuelRodHotDepleted,
                GCYNOrePrefix.fuelPellet,
                GCYNOrePrefix.fuelPelletDepleted,
                GCYNOrePrefix.dustSpentFuel,
                GCYNOrePrefix.dustBredFuel,
                GCYNOrePrefix.dustFissionByproduct
        );
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        GCYNLog.logger.info("Registering recipes...");

        GCYNRecipeManager.load();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerRecipesLowest(RegistryEvent.Register<IRecipe> event) {
        GCYNLog.logger.info("Running late material handlers...");

        GCYNRecipeManager.loadLatest();
    }

    public void preLoad() {
    }

    public void onPostLoad() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(GCYNPropertyKey.FISSION_FUEL)) {
                FissionFuelProperty prop = material.getProperty(GCYNPropertyKey.FISSION_FUEL);
                FissionFuelRegistry.registerFuel(OreDictUnifier.get(GCYNOrePrefix.fuelRod, material), prop,
                        OreDictUnifier.get(GCYNOrePrefix.fuelRodHotDepleted, material));
            }
            if (material.hasProperty(GCYNPropertyKey.COOLANT)) {
                CoolantProperty prop = material.getProperty(GCYNPropertyKey.COOLANT);
                CoolantRegistry.registerCoolant(material.getFluid(prop.getCoolantKey()), prop);
            }
        }
    }

    @SubscribeEvent
    public static void registerModuleContainer(ModuleContainerRegistryEvent event) {
        ModuleManager.getInstance().registerContainer(new GCYNModules());
    }
}
