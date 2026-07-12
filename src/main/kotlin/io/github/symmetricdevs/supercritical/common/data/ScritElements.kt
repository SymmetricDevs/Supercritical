package io.github.symmetricdevs.supercritical.common.data

import com.gregtechceu.gtceu.api.data.chemical.Element
import com.gregtechceu.gtceu.api.registry.GTRegistries
import com.gregtechceu.gtceu.common.data.GTElements
import io.github.symmetricdevs.supercritical.api.data.chemical.ElementExtension

/**
 * Supercritical's custom isotopes, declared GTElements-style: each is a `val` whose initializer
 * registers it into [GTRegistries.ELEMENTS]. Object initialization (forced by [init], called from
 * ScritAddon.registerElements during GTElements.init, i.e. before the registry freeze) runs every
 * initializer + the half-life setup. Half-lives are attached via the [ElementExtension] mixin.
 */
object ScritElements {

    val Uranium234 = createAndRegister(92, 142, -1, null, "Uranium-234", "U-234", true)
    val Uranium236 = createAndRegister(92, 144, -1, null, "Uranium-236", "U-236", true)
    val Uranium239 = createAndRegister(92, 147, -1, null, "Uranium-239", "U-239", true)
    val Neptunium235 = createAndRegister(93, 142, -1, null, "Neptunium-235", "Np-235", true)
    val Neptunium236 = createAndRegister(93, 143, -1, null, "Neptunium-236", "Np-236", true)
    val Neptunium237 = createAndRegister(93, 144, -1, null, "Neptunium-237", "Np-237", true)
    val Neptunium239 = createAndRegister(93, 146, -1, null, "Neptunium-239", "Np-239", true)
    val Plutonium238 = createAndRegister(94, 144, -1, null, "Plutonium-238", "Pu-238", true)
    val Plutonium240 = createAndRegister(94, 146, -1, null, "Plutonium-240", "Pu-240", true)
    val Plutonium242 = createAndRegister(94, 148, -1, null, "Plutonium-242", "Pu-242", true)
    val Plutonium244 = createAndRegister(94, 150, -1, null, "Plutonium-244", "Pu-244", true)
    val Americium241 = createAndRegister(95, 146, -1, null, "Americium-241", "Am-241", true)
    val Americium243 = createAndRegister(95, 148, -1, null, "Americium-243", "Am-243", true)
    val Curium244 = createAndRegister(96, 148, -1, null, "Curium-244", "Cm-244", true)
    val Curium245 = createAndRegister(96, 149, -1, null, "Curium-245", "Cm-245", true)
    val Curium246 = createAndRegister(96, 150, -1, null, "Curium-246", "Cm-246", true)

    // Runs on object init, after every val above is assigned.
    init {
        registerHalfLives()
    }

    /** Touch point so referencing ScritElements forces object initialization (cf. ScritOreVeins.init). */
    fun init() {}

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
        name: String, symbol: String, isIsotope: Boolean,
    ): Element {
        val element = Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope)
        GTRegistries.ELEMENTS.register(name, element)
        return element
    }
}
