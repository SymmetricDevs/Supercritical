package supercritical

import com.gregtechceu.gtceu.api.addon.GTAddon
import com.gregtechceu.gtceu.api.addon.IGTAddon
import com.gregtechceu.gtceu.api.data.chemical.Element
import com.gregtechceu.gtceu.api.registry.GTRegistries
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import supercritical.api.registries.SCRegistries

@GTAddon
class SCGTAddon : IGTAddon {
    override fun getRegistrate(): GTRegistrate {
        return SCRegistries.REGISTRATE
    }

    override fun initializeAddon() {}

    override fun addonModId(): String {
        return BuildConfig.MOD_ID
    }

    override fun registerTagPrefixes() {
    }

    override fun registerElements() {
        Uranium239 = createAndRegister(92, 147, -1, null, "Uranium-239", "U-239", true)
        Neptunium235 = createAndRegister(93, 142, -1, null, "Neptunium-235", "Np-235", true)
        Neptunium236 = createAndRegister(93, 143, -1, null, "Neptunium-236", "Np-236", true)
        Neptunium237 = createAndRegister(93, 144, -1, null, "Neptunium-237", "Np-237", true)
        Neptunium239 = createAndRegister(93, 146, -1, null, "Neptunium-239", "Np-239", true)
        Plutonium238 = createAndRegister(94, 144, -1, null, "Plutonium-238", "Pu-238", true)
        Plutonium240 = createAndRegister(94, 146, -1, null, "Plutonium-240", "Pu-240", true)
        Plutonium242 = createAndRegister(94, 148, -1, null, "Plutonium-242", "Pu-242", true)
        Plutonium244 = createAndRegister(94, 150, -1, null, "Plutonium-244", "Pu-244", true)
    }

    companion object {
        var Uranium239: Element? = null
        var Neptunium235: Element? = null
        var Neptunium236: Element? = null
        var Neptunium237: Element? = null
        var Neptunium239: Element? = null
        var Plutonium238: Element? = null
        var Plutonium240: Element? = null
        var Plutonium242: Element? = null
        var Plutonium244: Element? = null

        private fun createAndRegister(
            protons: Long, neutrons: Long, halfLifeSeconds: Long, decayTo: String?,
            name: String?, symbol: String?, isIsotope: Boolean
        ): Element {
            val element = Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope)
            GTRegistries.ELEMENTS.register<Element?>(name, element)
            return element
        }
    }
}
