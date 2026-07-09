package supercritical.common.metatileentities.multi.electric

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.BlockPattern
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import kotlin.math.max

class MetaTileEntityGasCentrifuge(holder: IMachineBlockEntity) : WorkableElectricMultiblockMachine(holder) {
    var columnCount: Int = 0
        private set

    override fun getPattern(): BlockPattern? {
        return super.getPattern()
    }

    override fun onStructureFormed() {
        super.onStructureFormed()
        val pattern = getPattern()
        val repetitions = if (pattern == null) null else pattern.getFormedRepetitionCount()
        if (repetitions != null && repetitions.size > 1) {
            this.columnCount = max(0, repetitions[1])
        } else {
            this.columnCount = 0
        }
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        this.columnCount = 0
    }

    companion object {
        fun recipeModifier(machine: MetaMachine?, recipe: GTRecipe?): ModifierFunction? {
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
