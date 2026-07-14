package io.github.symmetricdevs.supercritical.integration.xei.widgets

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture
import com.lowdragmc.lowdraglib.gui.widget.*
import com.lowdragmc.lowdraglib.jei.IngredientIO
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler
import io.github.symmetricdevs.supercritical.integration.xei.FissionFuelInfo
import kotlin.Boolean
import kotlin.apply
import kotlin.collections.forEachIndexed
import kotlin.collections.listOf
import kotlin.collections.plus
import kotlin.collections.toList
import kotlin.plus
import kotlin.sequences.plus
import kotlin.text.plus

/**
 * Shared, viewer-neutral widget for the fission fuel category: a fresh-rod input slot, a
 * cycling depleted-rod output slot, an animated progress arrow, and stat text.
 *
 * The slot widgets implement [com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot]
 * with [IngredientIO] set, so REI/EMI auto-extract them; JEI builds with [buildSlots] disabled.
 * The arrow is a [ProgressWidget] child so it renders through the LDLib ModularWrapper.
 */
class FissionFuelInfoWidget(
    info: FissionFuelInfo,
    buildSlots: Boolean = true
) : WidgetGroup(0, 0, WIDTH, HEIGHT) {

    init {
        if (buildSlots) {
            addWidget(
                SlotWidget(CycleItemStackHandler(listOf(listOf(info.rod))), 0, SLOT_INPUT_BG_X, SLOT_INPUT_BG_Y)
                    .setBackgroundTexture(GuiTextures.SLOT)
                    .setIngredientIO(IngredientIO.INPUT)
                    .setClientSideWidget()
            )
            addWidget(
                SlotWidget(
                    CycleItemStackHandler(listOf(info.depletedRods.toList())),
                    0, SLOT_OUTPUT_BG_X, SLOT_OUTPUT_BG_Y
                ).setBackgroundTexture(GuiTextures.SLOT)
                    .setIngredientIO(IngredientIO.OUTPUT)
                    .setClientSideWidget()
            )
        } else {
            addWidget(ImageWidget(SLOT_INPUT_BG_X, SLOT_INPUT_BG_Y, 18, 18, GuiTextures.SLOT))
            addWidget(ImageWidget(SLOT_OUTPUT_BG_X, SLOT_OUTPUT_BG_Y, 18, 18, GuiTextures.SLOT))
        }
        addWidget(
            ProgressWidget(
                ProgressWidget.JEIProgress,
                ARROW_X, ARROW_Y, ARROW_W, ARROW_H,
                ProgressTexture(
                    GuiTextures.PROGRESS_BAR_ARROW.getSubTexture(0.0, 0.0, 1.0, 0.5),
                    GuiTextures.PROGRESS_BAR_ARROW.getSubTexture(0.0, 0.5, 1.0, 0.5)
                ).apply { setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT) }
            )
        )
        info.textLines.forEachIndexed { index, line ->
            addWidget(LabelWidget(0, TEXT_Y + index * LINE_HEIGHT, line))
        }
    }

    companion object {
        const val WIDTH = 176
        const val HEIGHT = 90

        const val SLOT_INPUT_BG_X = 54
        const val SLOT_INPUT_BG_Y = 8
        const val SLOT_OUTPUT_BG_X = 104
        const val SLOT_OUTPUT_BG_Y = 8

        // Native ingredient slot positions (1px inset from the background).
        const val INGREDIENT_INPUT_X = 55
        const val INGREDIENT_INPUT_Y = 9
        const val INGREDIENT_OUTPUT_X = 105
        const val INGREDIENT_OUTPUT_Y = 9

        const val ARROW_X = 77
        const val ARROW_Y = 6
        const val ARROW_W = 20
        const val ARROW_H = 20

        const val TEXT_Y = 40
        private const val LINE_HEIGHT = 10
    }
}
