package io.github.symmetricdevs.supercritical.common.machine.multiblock.fission

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.BlockPattern
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates.*
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import io.github.symmetricdevs.supercritical.api.pattern.ScritPredicates
import io.github.symmetricdevs.supercritical.api.pattern.ScritPredicates.fillFluid
import io.github.symmetricdevs.supercritical.common.data.ScritBlocks
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeModifiers
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeModifiers.maxParallel
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeTypes
import io.github.symmetricdevs.supercritical.common.machine.multiblock.electric.GasCentrifuge
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import io.github.symmetricdevs.supercritical.util.blocks
import io.github.symmetricdevs.supercritical.util.scId
import io.github.symmetricdevs.supercritical.util.self
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.translatable
import net.minecraft.world.level.material.Fluids
import java.util.function.Consumer
import com.gregtechceu.gtceu.GTCEu.id as gtId

/**
 * Spent fuel pool multiblock. Slowly cools spent nuclear fuel by submerging it in water.
 * Pool length is variable and determines maximum recipe parallelism.
 */
class SpentFuelPool(holder: IMachineBlockEntity, vararg args: Any?) :
    WorkableMultiblockMachine(holder, *args), IControllable, IFancyUIMachine {
    override fun isRemote(): Boolean = super<WorkableMultiblockMachine>.isRemote()
    var waterFilled: Boolean = false
        private set
    private var waterPositions: MutableList<BlockPos>? = null
    private var waterFillSubscription: TickableSubscription? = null

    @Persisted
    @DescSynced
    var poolLength: Int = 0
        private set

    override fun onStructureFormed() {
        super.onStructureFormed()

        val wp = multiblockState.matchContext
            .getOrDefault<MutableList<BlockPos>>(ScritPredicates.FLUID_TO_FILL, arrayListOf())
        this.waterPositions = wp
        wp.sortWith(compareBy { - it.y })
        this.waterFilled = wp.isEmpty()
        this.waterFillSubscription = subscribeServerTick { this.tryFillWater() }
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        this.poolLength = 0
        unsubscribe(waterFillSubscription)
        this.waterFillSubscription = null
        this.waterPositions = null
        this.waterFilled = false
    }

    private fun tryFillWater() {
        if (this.waterFilled) return
        if (offsetTimer % 5 != 0L) return
        val positions = this.waterPositions ?: return
        if (positions.isEmpty()) return
        fillFluid(this, positions, Fluids.WATER)
        if (positions.isEmpty()) {
            this.waterFilled = true
            unsubscribe(waterFillSubscription)
        }
    }

    override fun isRecipeLogicAvailable(): Boolean {
        return super.isRecipeLogicAvailable() && waterFilled
    }

    fun addDisplayText(textList: MutableList<Component?>) {
        if (isFormed()) {
            if (!this.waterFilled) {
                textList.add(translatable("supercritical.multiblock.spent_fuel_pool.obstructed"))
            } else if (!isWorkingEnabled) {
                textList.add(translatable("gtceu.multiblock.work_paused"))
            } else if (recipeLogic.isActive()) {
                textList.add(translatable("gtceu.multiblock.running"))
            } else {
                textList.add(translatable("gtceu.multiblock.idling"))
            }
            textList.add(
                translatable(
                    "supercritical.multiblock.spent_fuel_pool.parallel",
                    poolLength * PARALLEL_PER_LENGTH
                )
            )
        }
    }

    override fun createUIWidget(): Widget {
        // Fancy UI main page: only the scrollable DISPLAY status screen (no gauges/slider —
        // SpentFuelPool just shows obstructed/running/idling + parallel text). IFancyUIMachine's
        // default createUI wraps this widget in a FancyMachineUIWidget, which supplies the standard
        // background, title bar, side tabs, configurators, and player inventory. createUI is
        // intentionally NOT overridden.
        val isClient = self().level?.isClientSide == true
        val group = WidgetGroup(0, 0, 190, 125)
        val screen = DraggableScrollableWidgetGroup(4, 4, 182, 117)
        screen.setBackground(GuiTextures.DISPLAY)
        screen.addWidget(LabelWidget(4, 5, self().definition.descriptionId))
        screen.addWidget(
            ComponentPanelWidget(4, 17, Consumer { text: MutableList<Component?> -> this.addDisplayText(text) })
                .textSupplier(if (isClient) null else Consumer { text: MutableList<Component?> ->
                    this.addDisplayText(text)
                })
                .setMaxWidthLimit(174)
        )
        group.addWidget(screen)
        return group
    }

    override fun checkPattern(): Boolean {
        val pattern = this.pattern
        pattern?.checkPatternAt(multiblockState, false)?.takeIf { it } ?: return false
        poolLength = pattern.formedRepetitionCount
            ?.getOrNull(2)
            ?: 0
        return true
    }

    companion object {
        const val PARALLEL_PER_LENGTH: Int = 32

        private fun buildPattern(definition: MultiblockMachineDefinition): BlockPattern {
            return FactoryBlockPattern.start(
                RelativeDirection.BACK,
                RelativeDirection.UP,
                RelativeDirection.RIGHT
            )
                .aisle(
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "TTTTTTTTTT"
                )
                .aisle(
                    "CCCCCCCCCC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "S........T"
                )
                .aisle(
                    "CCCCCCCCCC",
                    "CWRRRRRRWC",
                    "CWRRRRRRWC",
                    "CWRRRRRRWC",
                    "CWRRRRRRWC",
                    "CWRRRRRRWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "T........T"
                )
                .setRepeatable(1, 10)
                .aisle(
                    "CCCCCCCCCC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "CWWWWWWWWC",
                    "T........T"
                )
                .aisle(
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "CCCCCCCCCC",
                    "TTTTTTTTTT"
                )
                .where('S', definition.self)
                .where('.', any())
                .where('C', blockTag(ScritBlocks.PANELLING_TAG))
                .where('W', ScritPredicates.fluidFill(Fluids.WATER))
                .where('R', blocks(ScritBlocks.SPENT_FUEL_CASING))
                .where(
                    'T', blocks(GTBlocks.CASING_STAINLESS_CLEAN)
                        .or(autoAbilities(ScritRecipeTypes.SPENT_FUEL_POOL))
                        .or(autoAbilities(false, false, false))
                )
                .build()
        }

        fun register(): MultiblockMachineDefinition = ScritRegistration.REGISTRATE
            .multiblock("spent_fuel_pool") { SpentFuelPool(it) }
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(ScritRecipeTypes.SPENT_FUEL_POOL)
            .recipeModifiers(ScritRecipeModifiers.of<SpentFuelPool> { maxParallel(poolLength * PARALLEL_PER_LENGTH) })
            .pattern { buildPattern(it) }
            .workableCasingModel(
                gtId("block/casings/solid/machine_casing_clean_stainless_steel"),
                scId("block/multiblock/spent_fuel_pool")
            )
            .tooltipBuilder { _, tooltip ->
                tooltip.add(
                    translatable(
                        "supercritical.machine.spent_fuel_pool.tooltip.parallel",
                        PARALLEL_PER_LENGTH
                    )
                )
                tooltip.add(translatable("supercritical.machine.fluid_auto_fill.tooltip"))
            }
            .register()
    }
}
