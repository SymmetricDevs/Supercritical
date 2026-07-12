package io.github.symmetricdevs.supercritical

import com.gregtechceu.gtceu.api.addon.GTAddon
import com.gregtechceu.gtceu.api.addon.IGTAddon
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorFamilyRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.families.LegacyPWRFamily
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.geometry.SquareLattice
import io.github.symmetricdevs.supercritical.common.data.ScritTagPrefixes
import io.github.symmetricdevs.supercritical.common.data.ScritElements
import io.github.symmetricdevs.supercritical.common.data.ScritOreVeins
import io.github.symmetricdevs.supercritical.common.data.ScritRecipes
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import net.minecraft.data.recipes.FinishedRecipe
import java.util.function.Consumer

@GTAddon
class ScritAddon : IGTAddon {
    override fun getRegistrate(): GTRegistrate {
        return ScritRegistration.REGISTRATE
    }

    override fun addonModId(): String {
        return BuildConfig.MOD_ID
    }

    override fun initializeAddon() {
        ReactorFamilyRegistry.register(LegacyPWRFamily.id) { geometry, tag ->
            val lattice = geometry as SquareLattice
            val core = FissionReactor(lattice.size, lattice.depth, 1.0)
            if (tag != null) core.load(tag)
            core
        }
    }

    override fun registerTagPrefixes() {
        ScritTagPrefixes.init()
    }

    override fun registerOreVeins() {
        ScritOreVeins.init()
    }

    override fun registerElements() {
        ScritElements.init()
    }

    override fun addRecipes(provider: Consumer<FinishedRecipe>) {
        ScritRecipes.init(provider)
    }
}
