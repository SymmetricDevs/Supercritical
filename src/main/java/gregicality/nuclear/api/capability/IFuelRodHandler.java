package gregicality.nuclear.api.capability;

import net.minecraft.item.ItemStack;

import gregicality.nuclear.api.items.itemhandlers.LockableItemStackHandler;
import gregicality.nuclear.api.nuclear.fission.IFissionFuelStats;
import gregicality.nuclear.api.nuclear.fission.components.FuelRod;

public interface IFuelRodHandler extends ILockableHandler<ItemStack> {

    IFissionFuelStats getFuel();

    void setFuel(IFissionFuelStats prop);

    IFissionFuelStats getPartialFuel();

    /**
     * Set the fuel type that's currently being processed by this specific handler.
     * 
     * @param prop The new fuel type.
     * @return true if the partial fuel changed.
     */
    boolean setPartialFuel(IFissionFuelStats prop);

    void setInternalFuelRod(FuelRod rod);

    LockableItemStackHandler getStackHandler();
}
