package supercritical

import com.gregtechceu.gtceu.api.addon.GTAddon
import com.gregtechceu.gtceu.api.addon.IGTAddon
import com.gregtechceu.gtceu.api.data.chemical.Element
import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator
import com.gregtechceu.gtceu.api.registry.GTRegistries
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.gregtechceu.gtceu.common.data.GTElements
import com.gregtechceu.gtceu.common.data.GTOres
import net.minecraft.tags.BiomeTags
import net.minecraft.util.valueproviders.UniformInt
import supercritical.api.registries.ScritRegistries
import supercritical.api.unification.ElementExtension
import supercritical.api.unification.material.ScritMaterials
import supercritical.api.unification.ore.ScritOrePrefix
import supercritical.api.util.scId

@GTAddon
class ScritAddon : IGTAddon {
    override fun getRegistrate(): GTRegistrate {
        return ScritRegistries.REGISTRATE
    }

    override fun initializeAddon() {}

    override fun addonModId(): String {
        return BuildConfig.MOD_ID
    }

    override fun registerTagPrefixes() {
        ScritOrePrefix.init()
    }

    override fun registerOreVeins() {
        // Modern GTCEu does NOT auto-generate ore from .ore(); an explicit vein must be registered.
        // Zircon is SC's rare gem ore (legacy 1.12.2 spawned it via .ore() small-ore worldgen in the overworld).
        // No explicit legacy vein parameters existed, so sensible rare-gem defaults are used here.
        GTOres.create(scId("zircon_vein")) { vein ->
            vein.clusterSize(UniformInt.of(24, 32))
                .density(0.2f)
                .weight(20)
                .layer(WorldGenLayers.STONE)
                .heightRangeUniform(10, 60)
                .biomes(BiomeTags.IS_OVERWORLD)
                .classicVeinGenerator { generator ->
                    generator
                        .primary { it.mat(ScritMaterials.Zircon).size(3) }
                        .secondary { it.mat(ScritMaterials.Zircon).size(2) }
                        .between { it.mat(ScritMaterials.Zircon).size(2) }
                        .sporadic { it.mat(ScritMaterials.Zircon) }
                }
                .surfaceIndicatorGenerator {
                    it.surfaceRock(ScritMaterials.Zircon)
                        .placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)
                }
        }
    }

    override fun registerElements() {
        Uranium234 = createAndRegister(92, 142, -1, null, "Uranium-234", "U-234", true)
        Uranium236 = createAndRegister(92, 144, -1, null, "Uranium-236", "U-236", true)
        Uranium239 = createAndRegister(92, 147, -1, null, "Uranium-239", "U-239", true)
        Neptunium235 = createAndRegister(93, 142, -1, null, "Neptunium-235", "Np-235", true)
        Neptunium236 = createAndRegister(93, 143, -1, null, "Neptunium-236", "Np-236", true)
        Neptunium237 = createAndRegister(93, 144, -1, null, "Neptunium-237", "Np-237", true)
        Neptunium239 = createAndRegister(93, 146, -1, null, "Neptunium-239", "Np-239", true)
        Plutonium238 = createAndRegister(94, 144, -1, null, "Plutonium-238", "Pu-238", true)
        Plutonium240 = createAndRegister(94, 146, -1, null, "Plutonium-240", "Pu-240", true)
        Plutonium242 = createAndRegister(94, 148, -1, null, "Plutonium-242", "Pu-242", true)
        Plutonium244 = createAndRegister(94, 150, -1, null, "Plutonium-244", "Pu-244", true)
        Americium241 = createAndRegister(95, 146, -1, null, "Americium-241", "Am-241", true)
        Americium243 = createAndRegister(95, 148, -1, null, "Americium-243", "Am-243", true)
        Curium244 = createAndRegister(96, 148, -1, null, "Curium-244", "Cm-244", true)
        Curium245 = createAndRegister(96, 149, -1, null, "Curium-245", "Cm-245", true)
        Curium246 = createAndRegister(96, 150, -1, null, "Curium-246", "Cm-246", true)

        registerHalfLives()
    }

    companion object {
        lateinit var Uranium234: Element
        lateinit var Uranium236: Element
        lateinit var Uranium239: Element
        lateinit var Neptunium235: Element
        lateinit var Neptunium236: Element
        lateinit var Neptunium237: Element
        lateinit var Neptunium239: Element
        lateinit var Plutonium238: Element
        lateinit var Plutonium240: Element
        lateinit var Plutonium242: Element
        lateinit var Plutonium244: Element
        lateinit var Americium241: Element
        lateinit var Americium243: Element
        lateinit var Curium244: Element
        lateinit var Curium245: Element
        lateinit var Curium246: Element

        private fun registerHalfLives() {
            setHalfLife(GTElements.U, 1.4090285e+17)
            setHalfLife(Uranium234, 7.7472253e+12)
            setHalfLife(Uranium236, 7.4046528e+14)
            setHalfLife(GTElements.U238, 1.4090285e+17)
            setHalfLife(GTElements.U235, 2.2195037e+16)
            setHalfLife(Uranium239, 1407.0)

            setHalfLife(Neptunium235, 34223040.0)
            setHalfLife(Neptunium236, 1.33056e+10)
            setHalfLife(Neptunium237, 6.76801391e+13)
            setHalfLife(Neptunium239, 66200371.0)

            setHalfLife(Plutonium238, 2765707200.0)
            setHalfLife(GTElements.Pu239, 760332960000.0)
            setHalfLife(Plutonium240, 206907696000.0)
            setHalfLife(GTElements.Pu241, 450649440.0)
            setHalfLife(Plutonium242, 1.1826e+13)
            setHalfLife(Plutonium244, 2.52288e+15)

            setHalfLife(Americium241, 1.36515262e+10)
            setHalfLife(Americium243, 2.31943406e+11)

            setHalfLife(Curium244, 571590600.0)
            setHalfLife(Curium245, 2.60366729e+13)
            setHalfLife(Curium246, 1.48519516e+13)
        }

        private fun setHalfLife(element: Element, halfLifeSeconds: Double) {
            (element as ElementExtension).halfLifeSeconds = halfLifeSeconds
        }

        private fun createAndRegister(
            protons: Long, neutrons: Long, halfLifeSeconds: Long, decayTo: String?,
            name: String, symbol: String, isIsotope: Boolean
        ): Element {
            val element = Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope)
            GTRegistries.ELEMENTS.register(name, element)
            return element
        }
    }
}
