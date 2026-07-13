package io.github.symmetricdevs.supercritical.common.machine.multiblock.electric

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import io.github.symmetricdevs.supercritical.util.nullWrongType

class GasCentrifuge(holder: IMachineBlockEntity) : WorkableElectricMultiblockMachine(holder) {

    @Persisted
    @DescSynced
    var columnCount: Int = 0
        private set

    override fun onStructureFormed() {
        super.onStructureFormed()
        columnCount = pattern
            ?.formedRepetitionCount
            ?.getOrNull(1)
            ?: 0
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        columnCount = 0
    }

    companion object {
        val RECIPE_MODIFIER = fun(machine: MetaMachine, recipe: GTRecipe): ModifierFunction {
            if (machine !is GasCentrifuge) return nullWrongType<GasCentrifuge>(machine)

            val parallel = machine.columnCount
            if (parallel <= 1) return ModifierFunction.IDENTITY

            return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallel.toDouble()))
                .parallels(parallel)
                .build()
        }

        fun register() {

        }
    }
}
