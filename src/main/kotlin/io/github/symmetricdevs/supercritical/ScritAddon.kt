package io.github.symmetricdevs.supercritical

import com.gregtechceu.gtceu.api.addon.GTAddon
import com.gregtechceu.gtceu.api.addon.IGTAddon
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import io.github.symmetricdevs.supercritical.BuildConfig
import io.github.symmetricdevs.supercritical.api.data.chemical.ore.ScritOrePrefix
import io.github.symmetricdevs.supercritical.common.data.ScritElements
import io.github.symmetricdevs.supercritical.common.data.ScritOreVeins
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration

@GTAddon
class ScritAddon : IGTAddon {
    override fun getRegistrate(): GTRegistrate {
        return ScritRegistration.REGISTRATE
    }

    override fun initializeAddon() {}

    override fun addonModId(): String {
        return BuildConfig.MOD_ID
    }

    override fun registerTagPrefixes() {
        ScritOrePrefix.init()
    }

    override fun registerOreVeins() {
        ScritOreVeins.init()
    }

    override fun registerElements() {
        ScritElements.init()
    }
}
