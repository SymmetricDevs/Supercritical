package io.github.symmetricdevs.supercritical.integration.emi.basic

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiStack
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.xei.CoolantInfo
import io.github.symmetricdevs.supercritical.integration.xei.widgets.CoolantInfoWidget
import io.github.symmetricdevs.supercritical.util.scId
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

/**
 * EMI recipe for a coolant heating entry. Extends LDLib's [ModularEmiRecipe], which auto-extracts
 * the widget tree's [com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot] tanks into EMI
 * slots and auto-renders the widget via [com.lowdragmc.lowdraglib.jei.ModularWrapper].
 *
 * The recipe id path is `/`-prefixed because these entries are synthetic (not backed by a recipe in
 * the vanilla RecipeManager) — EMI flags unprefixed synthetic ids in the log.
 */
class CoolantEmiRecipe(private val info: CoolantInfo) :
    ModularEmiRecipe<WidgetGroup>({ CoolantInfoWidget(info) }) {

    override fun getCategory(): EmiRecipeCategory = CoolantEmiCategory

    override fun getId(): ResourceLocation {
        val key = ForgeRegistries.FLUIDS.getKey(info.coolant.fluid)
        return scId("/coolant/${key?.namespace ?: "minecraft"}/${key?.path ?: "unknown"}")
    }
}

object CoolantEmiCategory :
    EmiRecipeCategory(scId("coolant"), EmiStack.of(ScritMachines.FISSION_REACTOR.asStack())) {
    // Reuse the existing lang key (same title the JEI category shows) instead of EMI's default
    // `emi.category.supercritical.coolant`, which has no translation.
    override fun getName(): Component = Component.translatable("fission.coolant.name")
}
