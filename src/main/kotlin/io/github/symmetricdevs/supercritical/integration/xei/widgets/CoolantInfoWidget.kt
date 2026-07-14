package io.github.symmetricdevs.supercritical.integration.xei.widgets

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture
import com.lowdragmc.lowdraglib.gui.widget.*
import com.lowdragmc.lowdraglib.jei.IngredientIO
import com.lowdragmc.lowdraglib.misc.FluidStorage
import com.lowdragmc.lowdraglib.side.fluid.FluidStack
import io.github.symmetricdevs.supercritical.integration.xei.CoolantInfo
import kotlin.Boolean
import kotlin.apply
import kotlin.collections.forEachIndexed
import kotlin.collections.plus
import kotlin.plus
import kotlin.sequences.plus
import kotlin.text.plus

/**
 * Shared, viewer-neutral widget for the coolant heating category: a cold/hot coolant tank
 * pair, an animated progress arrow, and stat text.
 *
 * The tank widgets implement [com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot]
 * with [IngredientIO] set, so REI ([com.lowdragmc.lowdraglib.rei.ModularDisplay]) and EMI
 * ([com.lowdragmc.lowdraglib.emi.ModularEmiRecipe]) auto-extract them as native ingredient
 * slots and auto-render the whole tree via the LDLib [com.lowdragmc.lowdraglib.jei.ModularWrapper].
 * The arrow is a [ProgressWidget] child (not a manual draw) so it renders through that wrapper.
 * JEI builds the widget with [buildSlots] disabled and registers its own native slots on top.
 */
class CoolantInfoWidget(
    info: CoolantInfo,
    buildSlots: Boolean = true
) : WidgetGroup(0, 0, WIDTH, HEIGHT) {

    init {
        if (buildSlots) {
            addWidget(
                TankWidget(
                    FluidStorage(FluidStack.create(info.coolant.fluid, info.coolant.amount.toLong())),
                    SLOT_INPUT_BG_X, SLOT_INPUT_BG_Y, 18, 18, false, false
                ).setBackground(GuiTextures.SLOT)
                    .setIngredientIO(IngredientIO.INPUT)
                    .setClientSideWidget()
            )
            addWidget(
                TankWidget(
                    FluidStorage(FluidStack.create(info.hotCoolant.fluid, info.hotCoolant.amount.toLong())),
                    SLOT_OUTPUT_BG_X, SLOT_OUTPUT_BG_Y, 18, 18, false, false
                ).setBackground(GuiTextures.SLOT)
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
