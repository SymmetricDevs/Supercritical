package supercritical.common.registry

import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import supercritical.BuildConfig
import supercritical.common.data.ScritItems
import java.util.*
import java.util.Map

object ScritBlocks {
    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, BuildConfig.MOD_ID)

    val REACTOR_VESSEL: RegistryObject<Block> = registerFissionCasing("reactor_vessel")
    val FUEL_CHANNEL: RegistryObject<Block> = registerFissionCasing("fuel_channel")
    val CONTROL_ROD_CHANNEL: RegistryObject<Block> = registerFissionCasing("control_rod_channel")
    val COOLANT_CHANNEL: RegistryObject<Block> = registerFissionCasing("coolant_channel")

    val SPENT_FUEL_CASING: RegistryObject<Block> = registerNuclearCasing("spent_fuel_casing")
    val GAS_CENTRIFUGE_HEATER: RegistryObject<Block> = registerNuclearCasing("gas_centrifuge_heater")
    val GAS_CENTRIFUGE_COLUMN: RegistryObject<Block> = registerGasCentrifugeCasing("gas_centrifuge_column")

    val PANELLING: MutableMap<DyeColor, RegistryObject<Block>> = registerPanelling()
    val WHITE_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.WHITE])
    val ORANGE_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.ORANGE])
    val MAGENTA_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.MAGENTA])
    val LIGHT_BLUE_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.LIGHT_BLUE])
    val YELLOW_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.YELLOW])
    val LIME_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.LIME])
    val PINK_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.PINK])
    val GRAY_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.GRAY])
    val LIGHT_GRAY_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.LIGHT_GRAY])
    val CYAN_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.CYAN])
    val PURPLE_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.PURPLE])
    val BLUE_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.BLUE])
    val BROWN_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.BROWN])
    val GREEN_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.GREEN])
    val RED_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.RED])
    val BLACK_PANELLING: RegistryObject<Block> = requireNotNull(PANELLING[DyeColor.BLACK])

    fun register(modEventBus: IEventBus?) {
        BLOCKS.register(modEventBus)
    }

    private fun registerFissionCasing(name: String): RegistryObject<Block> {
        return registerSimpleBlock(
            name, BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .strength(10.0f, 10.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
        )
    }

    private fun registerNuclearCasing(name: String): RegistryObject<Block> {
        return registerSimpleBlock(
            name, BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .strength(5.0f, 10.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
        )
    }

    private fun registerGasCentrifugeCasing(name: String): RegistryObject<Block> {
        return registerSimpleBlock(
            name, BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .strength(5.0f, 10.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
        )
    }

    private fun registerPanelling(): MutableMap<DyeColor, RegistryObject<Block>> {
        val blocks = EnumMap<DyeColor, RegistryObject<Block>>(DyeColor::class.java)
        for (color in DyeColor.entries) {
            blocks[color] = registerSimpleBlock(
                color.getName() + "_panelling", BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .mapColor(color)
                    .strength(2.0f, 5.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
            )
        }
        return Map.copyOf(blocks)
    }

    private fun registerSimpleBlock(name: String, properties: BlockBehaviour.Properties): RegistryObject<Block> {
        val block = BLOCKS.register(name) { Block(properties) }
        ScritItems.ITEMS.register(name) { BlockItem(block.get(), Item.Properties()) }
        return block
    }
}
