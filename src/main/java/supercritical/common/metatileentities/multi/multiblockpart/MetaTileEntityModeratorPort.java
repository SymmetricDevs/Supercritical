package supercritical.common.metatileentities.multi.multiblockpart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.nuclear.fission.IModeratorStats;
import supercritical.api.nuclear.fission.ModeratorRegistry;
import supercritical.common.registry.SCBlocks;

public class MetaTileEntityModeratorPort extends TieredPartMachine implements IFissionReactorHatch {

    private IModeratorStats moderator;

    public IModeratorStats getModerator() {
        return moderator;
    }

    public MetaTileEntityModeratorPort(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public boolean checkValidity(int depth) {
        BlockPos.MutableBlockPos pos = getPos().mutable();
        Direction back = getFrontFacing().getOpposite();
        BlockState defaultState = getLevel().getBlockState(pos.move(back));
        IModeratorStats stats = ModeratorRegistry.getModerator(defaultState.getBlock());
        this.moderator = stats;
        if (stats == null) return false;
        for (int i = 2; i < depth; i++) {
            pos.move(back);
            if (!getLevel().getBlockState(pos).equals(defaultState)) {
                return false;
            }
        }
        pos.move(back);
        return getLevel().getBlockState(pos).getBlock() == SCBlocks.REACTOR_VESSEL.get();
    }
}
