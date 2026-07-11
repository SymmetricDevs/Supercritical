package supercritical.common.metatileentities.multi.electric

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction

class MetaTileEntityGasCentrifuge(holder: IMachineBlockEntity) : WorkableElectricMultiblockMachine(holder) {
    var columnCount: Int = 0
        private set

    override fun onStructureFormed() {
        super.onStructureFormed()
        this.columnCount = pattern
            ?.formedRepetitionCount
            ?.getOrNull(1)
            ?: 0
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        this.columnCount = 0
    }

    companion object {

        fun recipeModifier(machine: MetaMachine?): ModifierFunction {
            if (machine !is MetaTileEntityGasCentrifuge || !machine.isFormed()) {
                return ModifierFunction.IDENTITY
            }
            val parallel = machine.columnCount
            if (parallel <= 1) {
                return ModifierFunction.IDENTITY
            }
            return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallel.toDouble()))
                .eutMultiplier(parallel.toDouble())
                .parallels(parallel)
                .build()
        }
    }
}
