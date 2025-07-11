package supercritical.api.metatileentity.multiblock;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

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
}
