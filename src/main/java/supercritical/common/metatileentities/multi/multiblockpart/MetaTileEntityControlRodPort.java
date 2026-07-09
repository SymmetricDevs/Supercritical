package supercritical.common.metatileentities.multi.multiblockpart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.common.registry.SCBlocks;

public class MetaTileEntityControlRodPort extends TieredPartMachine implements IFissionReactorHatch {

    private final boolean hasModeratorTip;

    public boolean hasModeratorTip() {
        return hasModeratorTip;
    }

    public MetaTileEntityControlRodPort(IMachineBlockEntity holder, int tier, boolean hasModeratorTip) {
        super(holder, tier);
        this.hasModeratorTip = hasModeratorTip;
    }

    @Override
    public boolean checkValidity(int depth) {
        BlockPos.MutableBlockPos pos = getPos().mutable();
        Direction back = getFrontFacing().getOpposite();
        for (int i = 1; i < depth; i++) {
            pos.move(back);
            if (getLevel().getBlockState(pos).getBlock() != SCBlocks.CONTROL_ROD_CHANNEL.get()) {
                return false;
            }
        }
        pos.move(back);
        return getLevel().getBlockState(pos).getBlock() == SCBlocks.REACTOR_VESSEL.get();
    }
}
