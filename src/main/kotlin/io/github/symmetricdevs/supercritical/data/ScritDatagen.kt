package io.github.symmetricdevs.supercritical.data

import com.tterrag.registrate.providers.ProviderType
import com.tterrag.registrate.util.nullness.NonNullConsumer
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import io.github.symmetricdevs.supercritical.data.lang.ScritLangHandler
import io.github.symmetricdevs.supercritical.data.tags.FluidTagLoader
import io.github.symmetricdevs.supercritical.data.tags.ItemTagLoader

object ScritDatagen {
    fun init() {
        ScritRegistration.REGISTRATE.addDataGenerator(
            ProviderType.ITEM_TAGS,
            NonNullConsumer { ItemTagLoader.init(it) })
        ScritRegistration.REGISTRATE.addDataGenerator(
            ProviderType.FLUID_TAGS,
            NonNullConsumer { FluidTagLoader.init(it) })
        ScritRegistration.REGISTRATE.addDataGenerator(
            ProviderType.LANG,
            NonNullConsumer { ScritLangHandler.init(it) })
    }
}
