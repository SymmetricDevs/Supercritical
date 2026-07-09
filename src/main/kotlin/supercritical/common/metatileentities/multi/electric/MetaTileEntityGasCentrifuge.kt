package supercritical.common.metatileentities.multi.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;

public class MetaTileEntityGasCentrifuge extends WorkableElectricMultiblockMachine {

    private int columnCount;

    public MetaTileEntityGasCentrifuge(IMachineBlockEntity holder) {
        super(holder);
    }

    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public BlockPattern getPattern() {
        return super.getPattern();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        BlockPattern pattern = getPattern();
        int[] repetitions = pattern == null ? null : pattern.getFormedRepetitionCount();
        if (repetitions != null && repetitions.length > 1) {
            this.columnCount = Math.max(0, repetitions[1]);
        } else {
            this.columnCount = 0;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.columnCount = 0;
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof MetaTileEntityGasCentrifuge centrifuge) || !centrifuge.isFormed()) {
            return ModifierFunction.IDENTITY;
        }
        int parallel = centrifuge.getColumnCount();
        if (parallel <= 1) {
            return ModifierFunction.IDENTITY;
        }
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallel))
                .eutMultiplier(parallel)
                .parallels(parallel)
                .build();
    }
}
