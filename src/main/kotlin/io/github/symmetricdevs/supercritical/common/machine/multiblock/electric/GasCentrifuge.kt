package io.github.symmetricdevs.supercritical.common.machine.multiblock.electric

import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates.abilities
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import io.github.symmetricdevs.supercritical.common.data.ScritBlocks
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeModifiers
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeModifiers.maxParallel
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeTypes
import io.github.symmetricdevs.supercritical.common.machine.multiblock.fission.SpentFuelPool.Companion.PARALLEL_PER_LENGTH
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import io.github.symmetricdevs.supercritical.util.blocks
import io.github.symmetricdevs.supercritical.util.scId
import io.github.symmetricdevs.supercritical.util.self
import net.minecraft.network.chat.Component.translatable
import com.gregtechceu.gtceu.GTCEu.id as gtId

class GasCentrifuge(
    holder: IMachineBlockEntity,
) : WorkableElectricMultiblockMachine(holder) {
    @Persisted
    @DescSynced
    var columnCount: Int = 0
        private set

    override fun checkPattern(): Boolean {
        val pattern = this.pattern
        pattern?.checkPatternAt(multiblockState, false)?.takeIf { it } ?: return false
        columnCount = pattern.formedRepetitionCount
            ?.getOrNull(1)
            ?: 0
        return true
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        columnCount = 0
    }

    companion object {

        fun register(): MultiblockMachineDefinition =
            ScritRegistration.REGISTRATE
                .multiblock("gas_centrifuge") { GasCentrifuge(it) }
                .rotationState(RotationState.NON_Y_AXIS)
                .allowExtendedFacing(false)
                .recipeType(ScritRecipeTypes.GAS_CENTRIFUGE)
                .recipeModifiers(ScritRecipeModifiers.of<GasCentrifuge> { maxParallel(columnCount) })
                .pattern {
                    FactoryBlockPattern
                        .start(FRONT, UP, RIGHT)
                        .aisle("IS", "HH", "CC", "CC", "CC", "CC", "CC")
                        .aisle("EE", "HH", "CC", "CC", "CC", "CC", "CC")
                        .setRepeatable(1, 14)
                        .aisle("OO", "HH", "CC", "CC", "CC", "CC", "CC")
                        .where('S', it.self)
                        .where('H', blocks(ScritBlocks.GAS_CENTRIFUGE_HEATER))
                        .where('C', blocks(ScritBlocks.GAS_CENTRIFUGE_COLUMN))
                        .where(
                            'I',
                            blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE)
                                .or(abilities(PartAbility.IMPORT_FLUIDS)),
                        ).where(
                            'E',
                            blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE)
                                .or(abilities(PartAbility.INPUT_ENERGY))
                                .or(abilities(PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS)),
                        ).where(
                            'O',
                            abilities(PartAbility.EXPORT_FLUIDS)
                                .or(blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE)),
                        ).build()
                }.workableCasingModel(
                    gtId("block/casings/pipe/machine_casing_pipe_polytetrafluoroethylene"),
                    scId("block/multiblock/gas_centrifuge"),
                ).tooltipBuilder { _, tooltip ->
                    tooltip.add(translatable("supercritical.machine.gas_centrifuge.tooltip.parallel"))
                }.register()
    }
}
