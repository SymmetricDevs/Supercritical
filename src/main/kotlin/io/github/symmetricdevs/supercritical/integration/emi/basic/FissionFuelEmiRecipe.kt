package io.github.symmetricdevs.supercritical.integration.emi.basic

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiStack
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.xei.FissionFuelInfo
import io.github.symmetricdevs.supercritical.integration.xei.widgets.FissionFuelInfoWidget
import io.github.symmetricdevs.supercritical.util.scId
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

/** EMI recipe for a fission fuel rod entry. See [CoolantEmiRecipe] for the contract. */
class FissionFuelEmiRecipe(private val info: FissionFuelInfo) :
    ModularEmiRecipe<WidgetGroup>({ FissionFuelInfoWidget(info) }) {

    override fun getCategory(): EmiRecipeCategory = FissionFuelEmiCategory

    override fun getId(): ResourceLocation {
        val key = ForgeRegistries.ITEMS.getKey(info.rod.item)
        return scId("/fission_fuel/${key?.namespace ?: "minecraft"}/${key?.path ?: "unknown"}")
    }
}

object FissionFuelEmiCategory :
    EmiRecipeCategory(scId("fission_fuel"), EmiStack.of(ScritMachines.FISSION_REACTOR.asStack())) {
    override fun getName(): Component = Component.translatable("fission.fuel.name")
}
