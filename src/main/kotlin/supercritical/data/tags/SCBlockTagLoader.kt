package supercritical.data.tags

import com.tterrag.registrate.providers.RegistrateTagsProvider.IntrinsicImpl
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import supercritical.common.registry.ScritBlocks

object SCBlockTagLoader {
    // Legacy VariantBlock casings declared a wrench harvest tool
    // (BlockFissionCasing used ToolClasses.WRENCH, BlockPanelling used setHarvestLevel("wrench", 2));
    // the modern GTCEu convention is forge:mineable/wrench (GTCEu CustomTags.MINEABLE_WITH_WRENCH).
    // The casing blocks call requiresCorrectToolForDrops(), so without a mineable tag no tool
    // qualifies and they drop nothing.
    private val MINEABLE_WITH_WRENCH: TagKey<Block> =
        TagKey.create(Registries.BLOCK, ResourceLocation("forge", "mineable/wrench"))

    fun init(provider: IntrinsicImpl<Block>?) {
        val wrench = provider?.addTag(MINEABLE_WITH_WRENCH) ?: return
        // Fission casing variants.
        wrench.add(ScritBlocks.REACTOR_VESSEL.get())
        wrench.add(ScritBlocks.FUEL_CHANNEL.get())
        wrench.add(ScritBlocks.CONTROL_ROD_CHANNEL.get())
        wrench.add(ScritBlocks.COOLANT_CHANNEL.get())
        // Nuclear / gas-centrifuge casings.
        wrench.add(ScritBlocks.SPENT_FUEL_CASING.get())
        wrench.add(ScritBlocks.GAS_CENTRIFUGE_HEATER.get())
        wrench.add(ScritBlocks.GAS_CENTRIFUGE_COLUMN.get())
        // Panelling, all 16 dye colors.
        for (block in ScritBlocks.PANELLING.values) {
            wrench.add(block.get())
        }
    }
}
