package supercritical.data

import com.tterrag.registrate.providers.ProviderType
import com.tterrag.registrate.providers.RegistrateItemTagsProvider
import com.tterrag.registrate.providers.RegistrateLangProvider
import com.tterrag.registrate.providers.RegistrateTagsProvider.IntrinsicImpl
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider
import com.tterrag.registrate.util.nullness.NonNullConsumer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid
import supercritical.common.registry.ScritRegistration
import supercritical.data.lang.ScritLangHandler
import supercritical.data.loot.ScritLootTableLoader
import supercritical.data.tags.ScritBlockTagLoader
import supercritical.data.tags.ScritFluidTagLoader
import supercritical.data.tags.ScritItemTagLoader

object ScritDatagen {
    fun init() {
        ScritRegistration.REGISTRATE.addDataGenerator<IntrinsicImpl<Block>?>(
            ProviderType.BLOCK_TAGS,
            NonNullConsumer { ScritBlockTagLoader.init(it) })
        ScritRegistration.REGISTRATE.addDataGenerator<RegistrateItemTagsProvider?>(
            ProviderType.ITEM_TAGS,
            NonNullConsumer { ScritItemTagLoader.init(it) })
        ScritRegistration.REGISTRATE.addDataGenerator<IntrinsicImpl<Fluid?>?>(
            ProviderType.FLUID_TAGS,
            NonNullConsumer { ScritFluidTagLoader.init(it) })
        ScritRegistration.REGISTRATE.addDataGenerator<RegistrateLangProvider?>(
            ProviderType.LANG,
            NonNullConsumer { ScritLangHandler.init(it) })
        ScritRegistration.REGISTRATE.addDataGenerator<RegistrateLootTableProvider?>(
            ProviderType.LOOT,
            NonNullConsumer { ScritLootTableLoader.init(it) })
    }
}
