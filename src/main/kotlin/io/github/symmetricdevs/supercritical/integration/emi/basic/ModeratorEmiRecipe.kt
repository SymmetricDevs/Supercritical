package io.github.symmetricdevs.supercritical.integration.emi.basic

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiStack
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.integration.xei.ModeratorInfo
import io.github.symmetricdevs.supercritical.integration.xei.widgets.ModeratorInfoWidget
import io.github.symmetricdevs.supercritical.util.scId
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

/** EMI recipe for a moderator block entry. See [CoolantEmiRecipe] for the contract. */
class ModeratorEmiRecipe(private val info: ModeratorInfo) :
    ModularEmiRecipe<WidgetGroup>({ ModeratorInfoWidget(info) }) {

    override fun getCategory(): EmiRecipeCategory = ModeratorEmiCategory

    override fun getId(): ResourceLocation {
        val key = ForgeRegistries.BLOCKS.getKey(info.blockState.block)
        return scId("/moderator/${key?.namespace ?: "minecraft"}/${key?.path ?: "unknown"}")
    }
}

object ModeratorEmiCategory :
    EmiRecipeCategory(scId("moderator"), EmiStack.of(ScritMachines.FISSION_REACTOR.asStack())) {
    override fun getName(): Component = Component.translatable("fission.moderator.name")
}
