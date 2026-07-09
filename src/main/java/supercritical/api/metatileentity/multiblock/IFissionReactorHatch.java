package supercritical.api.metatileentity.multiblock;

import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

import supercritical.common.metatileentities.multi.MetaTileEntityFissionReactor;

public interface IFissionReactorHatch {

    /**
     * @param depth The depth of the reactor that needs checking
     * @return If the channel directly below the hatch is valid or not
     */
    boolean checkValidity(int depth);

    default boolean canContinue(double depletion) {
        return true;
    }

    @Nullable
    BlockPos getPos();

    /**
     * Called by the controller when it forms so the hatch can store a weak reference.
     */
    default void setController(MetaTileEntityFissionReactor controller) {}

    @Nullable
    default MetaTileEntityFissionReactor getController() {
        return null;
    }

    /**
     * @return the stored controller reference, or null if none is stored.
     */
    default boolean hasController() {
        return getController() != null;
    }

    default boolean isLocked() {
        return false;
    }

    default void setLocked(boolean locked) {}
}
