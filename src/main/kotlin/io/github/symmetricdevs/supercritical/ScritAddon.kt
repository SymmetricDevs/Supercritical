package io.github.symmetricdevs.supercritical

import com.gregtechceu.gtceu.api.addon.GTAddon
import com.gregtechceu.gtceu.api.addon.IGTAddon
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.registration.ComponentTypeRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.registration.SystemRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorAddonEntrypoint
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorFamilyRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.families.LegacyPWRFamily
import io.github.symmetricdevs.supercritical.common.data.ScritTagPrefixes
import io.github.symmetricdevs.supercritical.common.data.ScritElements
import io.github.symmetricdevs.supercritical.common.data.ScritOreVeins
import io.github.symmetricdevs.supercritical.common.data.ScritRecipes
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import net.minecraft.data.recipes.FinishedRecipe
import java.util.ServiceLoader
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
        // Register built-in reactor component types and systems first.
        LegacyPWRFamily.registerComponents(ComponentTypeRegistry)

        // Discover addon-provided components, systems, and families.
        val loader = ServiceLoader.load(
            ReactorAddonEntrypoint::class.java,
            ReactorAddonEntrypoint::class.java.classLoader
        )
        for (entry in loader) {
            entry.registerComponents(ComponentTypeRegistry)
            entry.registerSystems(SystemRegistry)
            entry.registerReactorFamilies(ReactorFamilyRegistry)
        }

        // Register the built-in PWR family last so its factory is available.
        ReactorFamilyRegistry.register(LegacyPWRFamily.id) { size, depth, insertion, tag ->
            val core = FissionReactor(size, depth, insertion)
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
