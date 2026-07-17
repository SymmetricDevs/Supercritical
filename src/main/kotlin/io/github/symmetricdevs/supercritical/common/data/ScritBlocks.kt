package io.github.symmetricdevs.supercritical.common.data

import com.google.common.collect.ImmutableMap
import com.gregtechceu.gtceu.api.block.ActiveBlock
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties
import com.gregtechceu.gtceu.data.recipe.CustomTags
import com.tterrag.registrate.providers.DataGenContext
import com.tterrag.registrate.providers.RegistrateBlockstateProvider
import com.tterrag.registrate.util.entry.BlockEntry
import com.tterrag.registrate.util.nullness.NonNullBiConsumer
import io.github.symmetricdevs.supercritical.common.block.ScritColumnBlock
import io.github.symmetricdevs.supercritical.common.data.ScritBlocks.init
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import io.github.symmetricdevs.supercritical.util.scId
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.core.registries.Registries

/** Registrate blockstate datagen callback for a plain [Block] (see [BlockBuilder.blockstate]). */
private typealias BlockstateGen = NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider>

/**
 * Supercritical's plain blocks (7 casings + 16 dyed panelling variants), registered via GTCEu's
 * [com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate] the same way GTCEu registers its
 * own blocks in `GTBlocks`.
 *
 * Registrate auto-applies `defaultBlockstate` (cube_all model from `block/<name>` texture),
 * `defaultLoot` (drop-self), and `defaultLang` (title-cased English, e.g. `"reactor_vessel"` →
 * `"Reactor Vessel"`, `"white_panelling"` → `"White Panelling"`), and [simpleItem] co-registers
 * each block's BlockItem — so there is no separate ScritItems BlockItem registration.
 *
 * `BlockEntry<Block>` implements `Supplier<Block>`, so every `ScritBlocks.X.get()` call site is
 * unchanged from the old `RegistryObject<Block>` API. Object initialization (triggered by [init]
 * from the mod constructor) runs the `val` initializers, which register each entry.
 */
object ScritBlocks {
    private val REGISTRATE = ScritRegistration.REGISTRATE

    val REACTOR_VESSEL: BlockEntry<Block> = registerFissionCasing("reactor_vessel")
    val FUEL_CHANNEL: BlockEntry<Block> = registerFissionCasing("fuel_channel")
    val CONTROL_ROD_CHANNEL: BlockEntry<Block> = registerFissionCasing("control_rod_channel")
    val COOLANT_CHANNEL: BlockEntry<Block> = registerFissionCasing("coolant_channel")

    val SPENT_FUEL_CASING: BlockEntry<Block> =
        registerNuclearCasing("spent_fuel_casing", blockstate = ::activeCasingBlockstate)
    val GAS_CENTRIFUGE_HEATER: BlockEntry<Block> =
        registerNuclearCasing("gas_centrifuge_heater", blockstate = ::activeCasingBlockstate)
    val GAS_CENTRIFUGE_COLUMN: BlockEntry<Block> = registerGasCentrifugeCasing(
        "gas_centrifuge_column",
        blockFactory = { ScritColumnBlock(it) }
    ) { ctx, prov ->
        // Centered non-full-block pillar (10x16x10); #side on the 4 vertical faces, #end on top/bottom.
        val pillar = prov.models().getBuilder(ctx.name)
            .parent(prov.models().getExistingFile(ResourceLocation.fromNamespaceAndPath("minecraft", "block/block")))
            .texture("side", scId("block/gas_centrifuge_column_side"))
            .texture("end", scId("block/gas_centrifuge_column_end"))
            .element()
                .from(3f, 0f, 3f)
                .to(13f, 16f, 13f)
                .face(Direction.DOWN).texture("#end").cullface(Direction.DOWN).end()
                .face(Direction.UP).texture("#end").cullface(Direction.UP).end()
                .face(Direction.NORTH).texture("#side").end()
                .face(Direction.SOUTH).texture("#side").end()
                .face(Direction.WEST).texture("#side").end()
                .face(Direction.EAST).texture("#side").end()
            .end()
        prov.simpleBlock(ctx.entry, pillar)
    }

    val PANELLING: ImmutableMap<DyeColor, BlockEntry<Block>> = registerPanelling()

    /** Matches every dyed paneling variant — used by multiblock patterns that accept any colour. */
    val PANELLING_TAG: TagKey<Block> = TagKey.create(Registries.BLOCK, scId("panelling"))

    /** Base paneling color (the one recipes are built around); other colours are made by dyeing it. */
    val GRAY_PANELLING: BlockEntry<Block> = PANELLING.getValue(DyeColor.GRAY)

    /** Forces object initialization so every block entry registers via GTRegistrate. */
    fun init() {}

    private fun registerFissionCasing(name: String): BlockEntry<Block> = registerCasing(name, 10.0f, 10.0f)

    private fun registerNuclearCasing(
        name: String,
        blockstate: BlockstateGen? = null
    ): BlockEntry<Block> = registerGasCentrifugeCasing(name, noOcclusion = false, blockstate = blockstate)

    private fun registerGasCentrifugeCasing(
        name: String,
        noOcclusion: Boolean = true,
        blockFactory: (BlockBehaviour.Properties) -> Block = { ActiveBlock(it) },
        blockstate: BlockstateGen? = null
    ): BlockEntry<Block> =
        registerCasing(name, 5.0f, 10.0f, noOcclusion, blockFactory, blockstate)

    private fun registerCasing(
        name: String, strengthA: Float, strengthB: Float, noOcclusion: Boolean = false,
        blockFactory: (BlockBehaviour.Properties) -> Block = { Block(it) },
        blockstate: BlockstateGen? = null
    ): BlockEntry<Block> {
        val builder = REGISTRATE.block(name, blockFactory)
            .initialProperties { Blocks.IRON_BLOCK }
            .properties {
                it.strength(strengthA, strengthB)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .mapColor(MapColor.METAL)
                if (noOcclusion) it.noOcclusion()
                it
            }
            .tag(CustomTags.MINEABLE_WITH_WRENCH)
        return (if (blockstate != null) builder.blockstate(blockstate) else builder)
            .simpleItem()
            .register()
    }

    /**
     * Coil-style ACTIVE-gated blockstate generator (mirrors GTCEu's `GTModels.createCoilModel` but
     * for a `cube_column` casing). The block extends GTCEu [ActiveBlock], so any GTCEu multiblock
     * controller auto-enrols it (the pattern matcher collects every `instanceof ActiveBlock`) and
     * flips its `ACTIVE` property when the controller starts/stops working — exactly like heating
     * coils. `active=false` (default, idle) renders the plain `cube_column` base; `active=true`
     * (multiblock running) renders `supercritical:block/cube_2_layer/column`, a full-bright
     * `_side_bloom` / `_top_bloom` overlay (`shade:false` + light 15) on `cutout_mipped`.
     */
    private fun activeCasingBlockstate(ctx: DataGenContext<Block, Block>, prov: RegistrateBlockstateProvider) {
        val name = ctx.name
        // Inactive: plain cube_column (side + top). This is also the block's default appearance.
        val inactive = prov.models().cubeColumn(name, scId("block/${name}_side"), scId("block/${name}_top"))
        // Active: two-layer base + emissive bloom overlay (see cube_2_layer/column.json).
        val active = prov.models().withExistingParent("${name}_active", scId("block/cube_2_layer/column"))
            .renderType("cutout_mipped")
            .texture("bot_side", scId("block/${name}_side"))
            .texture("bot_end", scId("block/${name}_top"))
            .texture("top_side", scId("block/${name}_side_bloom"))
            .texture("top_end", scId("block/${name}_top_bloom"))
        prov.getVariantBuilder(ctx.entry)
            .partialState().with(GTBlockStateProperties.ACTIVE, false).modelForState().modelFile(inactive).addModel()
            .partialState().with(GTBlockStateProperties.ACTIVE, true).modelForState().modelFile(active).addModel()
    }

    private fun registerPanelling(): ImmutableMap<DyeColor, BlockEntry<Block>> =
        ImmutableMap.copyOf(DyeColor.entries.associateWith(::registerPanellingBlock))

    private fun registerPanellingBlock(color: DyeColor): BlockEntry<Block> =
        REGISTRATE.block(color.getName() + "_panelling") { Block(it) }
            .initialProperties { Blocks.IRON_BLOCK }
            .properties {
                it.strength(2.0f, 5.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .mapColor(color)
            }
            .tag(CustomTags.MINEABLE_WITH_WRENCH, PANELLING_TAG)
            .simpleItem()
            .register()
}
