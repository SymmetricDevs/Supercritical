package supercritical.data

import com.tterrag.registrate.providers.ProviderType
import com.tterrag.registrate.providers.RegistrateItemTagsProvider
import com.tterrag.registrate.providers.RegistrateLangProvider
import com.tterrag.registrate.providers.RegistrateTagsProvider.IntrinsicImpl
import com.tterrag.registrate.util.nullness.NonNullConsumer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid
import supercritical.api.registries.SCRegistries
import supercritical.data.lang.SCLangHandler
import supercritical.data.tags.SCBlockTagLoader
import supercritical.data.tags.SCFluidTagLoader
import supercritical.data.tags.SCItemTagLoader

object SCDatagen {
    fun init() {
        SCRegistries.REGISTRATE.addDataGenerator<IntrinsicImpl<Block?>?>(
            ProviderType.BLOCK_TAGS,
            NonNullConsumer { obj: IntrinsicImpl<Block?>? -> SCBlockTagLoader.init() })
        SCRegistries.REGISTRATE.addDataGenerator<RegistrateItemTagsProvider?>(
            ProviderType.ITEM_TAGS,
            NonNullConsumer { obj: RegistrateItemTagsProvider? -> SCItemTagLoader.init() })
        SCRegistries.REGISTRATE.addDataGenerator<IntrinsicImpl<Fluid?>?>(
            ProviderType.FLUID_TAGS,
            NonNullConsumer { obj: IntrinsicImpl<Fluid?>? -> SCFluidTagLoader.init() })
        SCRegistries.REGISTRATE.addDataGenerator<RegistrateLangProvider?>(
            ProviderType.LANG,
            NonNullConsumer { obj: RegistrateLangProvider? -> SCLangHandler.init() })
    }
}
