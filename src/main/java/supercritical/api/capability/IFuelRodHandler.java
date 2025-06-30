package supercritical.api.capability;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandlerModifiable;
import supercritical.api.items.itemhandlers.LockableItemStackHandler;
import supercritical.api.nuclear.fission.IFissionFuelStats;
import supercritical.api.nuclear.fission.components.FuelRod;

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

    double getDepletionPoint();
    boolean isDepleted(double totalDepletion);
    void markUndepleted();

    LockableItemStackHandler getInputStackHandler();
    IItemHandlerModifiable getOutputStackHandler(int depth);

    void resetDepletion(double fuelDepletion);
}
