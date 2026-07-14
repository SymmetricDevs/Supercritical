package io.github.symmetricdevs.supercritical.integration.xei.widgets

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.jei.IngredientIO
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler
import io.github.symmetricdevs.supercritical.integration.xei.ModeratorInfo

/**
 * Shared, viewer-neutral widget for the moderator category: a single moderator-block slot
 * and stat text.
 *
 * The slot widget implements [com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot]
 * with [IngredientIO] set, so REI/EMI auto-extract it; JEI builds with [buildSlots] disabled.
 */
class ModeratorInfoWidget(info: ModeratorInfo, buildSlots: Boolean = true) :
    WidgetGroup(0, 0, WIDTH, HEIGHT) {

    init {
        if (buildSlots) {
            addWidget(
                SlotWidget(CycleItemStackHandler(listOf(listOf(info.stack))), 0, SLOT_INPUT_BG_X, SLOT_INPUT_BG_Y)
                    .setBackgroundTexture(GuiTextures.SLOT)
                    .setIngredientIO(IngredientIO.INPUT)
                    .setClientSideWidget()
            )
        } else {
            addWidget(ImageWidget(SLOT_INPUT_BG_X, SLOT_INPUT_BG_Y, 18, 18, GuiTextures.SLOT))
        }
        info.textLines.forEachIndexed { index, line ->
            addWidget(LabelWidget(0, TEXT_Y + index * LINE_HEIGHT, line))
        }
    }

    companion object {
        const val WIDTH = 176
        const val HEIGHT = 70

        const val SLOT_INPUT_BG_X = 77
        const val SLOT_INPUT_BG_Y = 8

        // Native ingredient slot position (1px inset from the background).
        const val INGREDIENT_INPUT_X = 78
        const val INGREDIENT_INPUT_Y = 9

        const val TEXT_Y = 40
        private const val LINE_HEIGHT = 10
    }
}
