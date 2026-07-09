package supercritical.common.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import supercritical.BuildConfig;

import java.util.EnumMap;
import java.util.Map;

public final class SCBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BuildConfig.MOD_ID);

    public static final RegistryObject<Block> REACTOR_VESSEL = registerFissionCasing("reactor_vessel");
    public static final RegistryObject<Block> FUEL_CHANNEL = registerFissionCasing("fuel_channel");
    public static final RegistryObject<Block> CONTROL_ROD_CHANNEL = registerFissionCasing("control_rod_channel");
    public static final RegistryObject<Block> COOLANT_CHANNEL = registerFissionCasing("coolant_channel");

    public static final RegistryObject<Block> SPENT_FUEL_CASING = registerNuclearCasing("spent_fuel_casing");
    public static final RegistryObject<Block> GAS_CENTRIFUGE_HEATER = registerNuclearCasing("gas_centrifuge_heater");
    public static final RegistryObject<Block> GAS_CENTRIFUGE_COLUMN = registerGasCentrifugeCasing("gas_centrifuge_column");

    public static final Map<DyeColor, RegistryObject<Block>> PANELLING = registerPanelling();
    public static final RegistryObject<Block> WHITE_PANELLING = PANELLING.get(DyeColor.WHITE);
    public static final RegistryObject<Block> ORANGE_PANELLING = PANELLING.get(DyeColor.ORANGE);
    public static final RegistryObject<Block> MAGENTA_PANELLING = PANELLING.get(DyeColor.MAGENTA);
    public static final RegistryObject<Block> LIGHT_BLUE_PANELLING = PANELLING.get(DyeColor.LIGHT_BLUE);
    public static final RegistryObject<Block> YELLOW_PANELLING = PANELLING.get(DyeColor.YELLOW);
    public static final RegistryObject<Block> LIME_PANELLING = PANELLING.get(DyeColor.LIME);
    public static final RegistryObject<Block> PINK_PANELLING = PANELLING.get(DyeColor.PINK);
    public static final RegistryObject<Block> GRAY_PANELLING = PANELLING.get(DyeColor.GRAY);
    public static final RegistryObject<Block> LIGHT_GRAY_PANELLING = PANELLING.get(DyeColor.LIGHT_GRAY);
    public static final RegistryObject<Block> CYAN_PANELLING = PANELLING.get(DyeColor.CYAN);
    public static final RegistryObject<Block> PURPLE_PANELLING = PANELLING.get(DyeColor.PURPLE);
    public static final RegistryObject<Block> BLUE_PANELLING = PANELLING.get(DyeColor.BLUE);
    public static final RegistryObject<Block> BROWN_PANELLING = PANELLING.get(DyeColor.BROWN);
    public static final RegistryObject<Block> GREEN_PANELLING = PANELLING.get(DyeColor.GREEN);
    public static final RegistryObject<Block> RED_PANELLING = PANELLING.get(DyeColor.RED);
    public static final RegistryObject<Block> BLACK_PANELLING = PANELLING.get(DyeColor.BLACK);

    private SCBlocks() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }

    private static RegistryObject<Block> registerFissionCasing(String name) {
        return registerSimpleBlock(name, BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .strength(10.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
    }

    private static RegistryObject<Block> registerNuclearCasing(String name) {
        return registerSimpleBlock(name, BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
    }

    private static RegistryObject<Block> registerGasCentrifugeCasing(String name) {
        return registerSimpleBlock(name, BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    private static Map<DyeColor, RegistryObject<Block>> registerPanelling() {
        var blocks = new EnumMap<DyeColor, RegistryObject<Block>>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            blocks.put(color, registerSimpleBlock(color.getName() + "_panelling", BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .mapColor(color)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));
        }
        return Map.copyOf(blocks);
    }

    private static RegistryObject<Block> registerSimpleBlock(String name, BlockBehaviour.Properties properties) {
        var block = BLOCKS.register(name, () -> new Block(properties));
        SCItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}
