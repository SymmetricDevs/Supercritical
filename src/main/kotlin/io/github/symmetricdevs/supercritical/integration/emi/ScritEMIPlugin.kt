package io.github.symmetricdevs.supercritical.integration.emi

import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.stack.EmiStack
import io.github.symmetricdevs.supercritical.api.nuclear.fission.CoolantRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionFuelRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ModeratorRegistry
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.emi.basic.*
import io.github.symmetricdevs.supercritical.integration.xei.CoolantInfo
import io.github.symmetricdevs.supercritical.integration.xei.FissionFuelInfo
import io.github.symmetricdevs.supercritical.integration.xei.ModeratorInfo

/**
 * EMI integration. Discovered via the [@EmiEntrypoint] annotation, so this class must not be
 * referenced from the main mod class — EMI loads it only when present.
 *
 * Recipes are thin wrappers over LDLib's [com.lowdragmc.lowdraglib.emi.ModularEmiRecipe], which
 * auto-extracts the shared [io.github.symmetricdevs.supercritical.integration.xei.widgets] slots.
 */
@EmiEntrypoint
class ScritEMIPlugin : EmiPlugin {

    override fun register(registry: EmiRegistry) {
        registry.addCategory(CoolantEmiCategory)
        registry.addCategory(FissionFuelEmiCategory)
        registry.addCategory(ModeratorEmiCategory)

        val reactor = EmiStack.of(ScritMachines.FISSION_REACTOR.asStack())
        val moderatorPort = EmiStack.of(ScritMachines.MODERATOR_PORT.asStack())
        registry.addWorkstation(CoolantEmiCategory, reactor)
        registry.addWorkstation(FissionFuelEmiCategory, reactor)
        registry.addWorkstation(ModeratorEmiCategory, reactor)
        registry.addWorkstation(ModeratorEmiCategory, moderatorPort)

        for (coolant in CoolantRegistry.allCoolants) {
            val stats = CoolantRegistry.getCoolant(coolant) ?: continue
            val hotCoolant = stats.hotCoolant
            if (coolant != null && hotCoolant != null) {
                registry.addRecipe(CoolantEmiRecipe(CoolantInfo(coolant, hotCoolant)))
            }
        }
        for (rod in FissionFuelRegistry.allFissionableRods) {
            registry.addRecipe(FissionFuelEmiRecipe(FissionFuelInfo(rod)))
        }
        for (block in ModeratorRegistry.allModerators) {
            if (block != null) registry.addRecipe(ModeratorEmiRecipe(ModeratorInfo(block)))
        }
    }
}
