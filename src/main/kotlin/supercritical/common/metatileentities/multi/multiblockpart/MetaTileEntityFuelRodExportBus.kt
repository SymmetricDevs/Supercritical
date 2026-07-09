package supercritical.common.metatileentities.multi.multiblockpart;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;

import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;

public class MetaTileEntityFuelRodExportBus extends TieredIOPartMachine
        implements IControllable, IFissionReactorHatch {

    @Persisted
    @DescSynced
    @RequireRerender
    private boolean workingEnabled;
    private final NotifiableItemStackHandler inventory;

    public MetaTileEntityFuelRodExportBus(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.OUT);
        this.workingEnabled = true;
        this.inventory = new NotifiableItemStackHandler(this, 1, IO.OUT);
    }

    public NotifiableItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
    }

    @Override
    public boolean checkValidity(int depth) {
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(() -> {
            if (getOffsetTimer() % 5 == 0 && isWorkingEnabled()) {
                inventory.exportToNearby(getFrontFacing());
            }
        });
    }
}
