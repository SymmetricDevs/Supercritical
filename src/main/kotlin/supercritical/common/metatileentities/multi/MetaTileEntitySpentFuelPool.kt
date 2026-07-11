package supercritical.common.machine.multiblock

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.pattern.BlockPattern
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
import com.gregtechceu.gtceu.common.data.GTBlocks
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.level.material.Fluids
import supercritical.api.pattern.SCPredicates
import supercritical.api.recipes.SCRecipeMaps
import supercritical.api.registries.SCRegistries
import supercritical.api.util.scId
import supercritical.common.registry.SCBlocks
import kotlin.math.max

/**
 * Spent fuel pool multiblock. Slowly cools spent nuclear fuel by submerging it in water.
 * Pool length is variable and determines maximum recipe parallelism.
 */
class MetaTileEntitySpentFuelPool(holder: IMachineBlockEntity, vararg args: Any?) :
    WorkableMultiblockMachine(holder, *args), IControllable, IDisplayUIMachine {
    override fun isRemote(): Boolean = super<WorkableMultiblockMachine>.isRemote()
    private var workingEnabled = true
    var isWaterFilled: Boolean = false
        private set
    private var waterPositions: MutableList<BlockPos>? = null
    private var waterFillSubscription: TickableSubscription? = null
    var poolLength: Int = 1
        private set

    override fun isWorkingEnabled(): Boolean {
        return workingEnabled
    }

    override fun setWorkingEnabled(workingEnabled: Boolean) {
        this.workingEnabled = workingEnabled
    }

    override fun createRecipeLogic(vararg args: Any?): RecipeLogic {
        return SpentFuelPoolRecipeLogic(this)
    }

    override fun getRecipeLogic(): SpentFuelPoolRecipeLogic {
        return super.getRecipeLogic() as SpentFuelPoolRecipeLogic
    }

    override fun onStructureFormed() {
        super.onStructureFormed()
        val wp = multiblockState.matchContext
            .getOrDefault<ArrayList<BlockPos>>(SCPredicates.FLUID_BLOCKS_KEY, arrayListOf())
        this.waterPositions = wp
        wp.sortWith(compareBy { it.y })
        this.isWaterFilled = wp.isEmpty()
        val repetitions = pattern.getFormedRepetitionCount()
        this.poolLength = if (repetitions != null && repetitions.size > 2) max(1, repetitions[2]) else 1
        this.waterFillSubscription = subscribeServerTick { this.tryFillWater() }
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        unsubscribe(this.waterFillSubscription)
        this.waterFillSubscription = null
        this.waterPositions = null
        this.isWaterFilled = false
        this.poolLength = 1
    }

    private fun tryFillWater() {
        if (this.isWaterFilled) return
        val positions = this.waterPositions ?: return
        if (positions.isEmpty()) return
        if (offsetTimer % 5 != 0L) return
        SCPredicates.fillFluid(this, positions, Fluids.WATER)
        if (positions.isEmpty()) {
            this.isWaterFilled = true
        }
    }

    override fun isRecipeLogicAvailable(): Boolean {
        return super.isRecipeLogicAvailable() && this.isWaterFilled
    }

    val maxParallel: Int
        get() = this.poolLength * PARALLEL_PER_LENGTH

    override fun addDisplayText(textList: MutableList<Component?>) {
        super.addDisplayText(textList)
        if (isFormed()) {
            if (!this.isWaterFilled) {
                textList.add(Component.translatable("supercritical.multiblock.spent_fuel_pool.obstructed"))
            } else if (!isWorkingEnabled) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"))
            } else if (recipeLogic.isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"))
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"))
            }
            textList.add(
                Component.translatable(
                    "supercritical.multiblock.spent_fuel_pool.parallel",
                    this.maxParallel
                )
            )
        }
    }

    override fun getPattern(): BlockPattern {
        return buildPattern(definition)
    }

    class SpentFuelPoolRecipeLogic(machine: IRecipeLogicMachine) : RecipeLogic(machine) {
        override fun serverTick() {
            if (!machine.isWorkingEnabled) {
                return
            }
            super.serverTick()
        }

        override fun checkMatchedRecipeAvailable(match: GTRecipe?): Boolean {
            val pool = machine as? MetaTileEntitySpentFuelPool ?: return false
            if (!pool.isWaterFilled) {
                return false
            }
            val modified = pool.fullModifyRecipe(match)
            if (modified != null) {
                val recipeMatch = checkRecipe(modified)
                if (recipeMatch.isSuccess) {
                    setupRecipe(modified)
                }
                if (lastRecipe != null && status == Status.WORKING) {
                    lastOriginRecipe = match
                    lastFailedMatches = null
                    return true
                }
            }
            return false
        }
    }

    companion object {
        const val PARALLEL_PER_LENGTH: Int = 32

        private fun buildPattern(definition: MultiblockMachineDefinition): BlockPattern {
            return FactoryBlockPattern.start(
                RelativeDirection.BACK,
                RelativeDirection.UP,
                RelativeDirection.RIGHT
            ) // spotless:off
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
                ) //spotless:on
                .where('S', Predicates.controller(Predicates.blocks(definition.block)))
                .where('.', Predicates.any())
                .where('C', Predicates.blocks(SCBlocks.GRAY_PANELLING.get()))
                .where('W', SCPredicates.fluid(Fluids.WATER))
                .where('R', Predicates.blocks(SCBlocks.SPENT_FUEL_CASING.get()))
                .where(
                    'T', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                        .or(Predicates.autoAbilities(SCRecipeMaps.SPENT_FUEL_POOL_RECIPES))
                        .or(Predicates.autoAbilities(false, false, false))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS))
                )
                .build()
        }

        fun register(): MultiblockMachineDefinition {
            return SCRegistries.REGISTRATE
                .multiblock("spent_fuel_pool") { holder: IMachineBlockEntity -> MetaTileEntitySpentFuelPool(holder) }
                .rotationState(RotationState.NON_Y_AXIS)
                .allowExtendedFacing(false)
                .recipeType(SCRecipeMaps.SPENT_FUEL_POOL_RECIPES)
                .recipeModifiers(RecipeModifier { machine: MetaMachine, recipe: GTRecipe ->
                    poolParallel(machine, recipe)
                })
                .pattern { definition: MultiblockMachineDefinition -> buildPattern(definition) }
                .workableCasingModel(
                    scId("block/gray_panelling"),
                    scId("block/multiblock/spent_fuel_pool")
                )
                .tooltipBuilder { _, tooltip: MutableList<Component?> ->
                    tooltip.add(
                        Component.translatable(
                            "supercritical.machine.spent_fuel_pool.tooltip.parallel",
                            PARALLEL_PER_LENGTH
                        )
                    )
                    tooltip.add(Component.translatable("supercritical.machine.fluid_auto_fill.tooltip"))
                }
                .register()
        }

        /**
         * Recipe modifier that applies pool-length based parallelism.
         */
        fun poolParallel(machine: MetaMachine?, recipe: GTRecipe): ModifierFunction {
            if (machine !is MetaTileEntitySpentFuelPool || !machine.isFormed()) {
                return ModifierFunction.IDENTITY
            }
            val parallels = ParallelLogic.getParallelAmount(machine, recipe, machine.maxParallel)
            if (parallels <= 1) return ModifierFunction.IDENTITY
            return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels.toDouble()))
                .eutMultiplier(parallels.toDouble())
                .parallels(parallels)
                .build()
        }
    }
}
