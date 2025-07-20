package supercritical.common;

import java.util.Objects;
import java.util.function.Function;

import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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

import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantItemBlock;
import gregtech.api.modules.ModuleContainerRegistryEvent;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.event.MaterialRegistryEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;
import gregtech.common.items.MetaItems;
import gregtech.modules.ModuleManager;
import supercritical.SCValues;
import supercritical.api.nuclear.fission.CoolantRegistry;
import supercritical.api.nuclear.fission.FissionFuelRegistry;
import supercritical.api.nuclear.fission.ModeratorRegistry;
import supercritical.api.nuclear.fission.components.Moderator;
import supercritical.api.unification.material.properties.CoolantProperty;
import supercritical.api.unification.material.properties.FissionFuelProperty;
import supercritical.api.unification.material.properties.ModeratorProperty;
import supercritical.api.unification.material.properties.SCPropertyKey;
import supercritical.api.unification.ore.SCOrePrefix;
import supercritical.api.util.SCLog;
import supercritical.common.blocks.SCMetaBlocks;
import supercritical.common.item.SCMetaItems;
import supercritical.loaders.recipe.SCRecipeManager;
import supercritical.modules.SCModules;

@Mod.EventBusSubscriber(modid = SCValues.MODID)
public class CommonProxy {

    @SubscribeEvent
    public static void syncConfigValues(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(SCValues.MODID)) {
            ConfigManager.sync(SCValues.MODID, Config.Type.INSTANCE);
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        SCLog.logger.info("Registering blocks...");
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(SCMetaBlocks.FISSION_CASING);
        registry.register(SCMetaBlocks.NUCLEAR_CASING);
        registry.register(SCMetaBlocks.GAS_CENTRIFUGE_CASING);
        registry.register(SCMetaBlocks.PANELLING);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        SCLog.logger.info("Registering Items...");

        SCMetaItems.initSubitems();
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(createItemBlock(SCMetaBlocks.FISSION_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(SCMetaBlocks.NUCLEAR_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(SCMetaBlocks.GAS_CENTRIFUGE_CASING, VariantItemBlock::new));
        registry.register(createItemBlock(SCMetaBlocks.PANELLING, VariantItemBlock::new));
    }

    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        return itemBlock;
    }

    @SubscribeEvent
    public static void postRegisterMaterials(@NotNull PostMaterialEvent event) {
        MetaItems.addOrePrefix(
                SCOrePrefix.fuelRod,
                SCOrePrefix.fuelRodDepleted,
                SCOrePrefix.fuelRodHotDepleted,
                SCOrePrefix.fuelPelletRaw,
                SCOrePrefix.fuelPellet,
                SCOrePrefix.fuelPelletDepleted,
                SCOrePrefix.dustSpentFuel,
                SCOrePrefix.dustBredFuel,
                SCOrePrefix.dustFissionByproduct);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        SCLog.logger.info("Registering recipes...");

        SCRecipeManager.load();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerRecipesLowest(RegistryEvent.Register<IRecipe> event) {
        SCLog.logger.info("Running late material handlers...");

        SCRecipeManager.loadLatest();
    }

    public void preLoad() {}

    public void onPostLoad() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(SCPropertyKey.FISSION_FUEL)) {
                FissionFuelProperty prop = material.getProperty(SCPropertyKey.FISSION_FUEL);
                FissionFuelRegistry.registerFuel(OreDictUnifier.get(SCOrePrefix.fuelRod, material), prop,
                        OreDictUnifier.get(SCOrePrefix.fuelRodHotDepleted, material));
            }
            if (material.hasProperty(SCPropertyKey.COOLANT)) {
                CoolantProperty prop = material.getProperty(SCPropertyKey.COOLANT);
                CoolantRegistry.registerCoolant(material.getFluid(prop.getCoolantKey()), prop);
            }
            if (material.hasProperty(SCPropertyKey.MODERATOR)) {
                ModeratorProperty prop = material.getProperty(SCPropertyKey.MODERATOR);
                IBlockState state = MetaBlocks.COMPRESSED.get(material).getBlock(material);
                ModeratorRegistry.registerModerator(state, prop);
            }
        }
    }

    @SubscribeEvent
    public static void createMaterialRegistry(MaterialRegistryEvent event) {
        GregTechAPI.materialManager.createRegistry(SCValues.MODID);
    }

    @SubscribeEvent
    public static void registerModuleContainer(ModuleContainerRegistryEvent event) {
        ModuleManager.getInstance().registerContainer(new SCModules());
    }
}
