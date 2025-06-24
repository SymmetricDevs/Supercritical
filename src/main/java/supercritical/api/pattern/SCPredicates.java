package supercritical.api.pattern;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import supercritical.mixins.gregtech.AbstractRecipeLogicAccessor;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class SCPredicates {

    public static final String FLUID_BLOCKS_KEY = "FluidBlocks";

    public static TraceabilityPredicate fluid(FluidStack fluidStack) {
        return fluid(fluidStack.getFluid());
    }

    public static TraceabilityPredicate fluid(Fluid fluid) {
        Block fluidBlock = fluid.getBlock();
        String fluidName = fluid.getName();
        if (fluidBlock == null) {
            throw new IllegalArgumentException("Fluid \"" + fluidName + "\" has no associated block!");
        }
        IBlockState stillState = fluidBlock.getDefaultState();

        return new TraceabilityPredicate(
                bws -> {
                    IBlockState blockState = bws.getBlockState();
                    if (blockState == stillState) return true;
                    if (bws.getWorld().isAirBlock(bws.getPos()) || blockState.getBlock() == fluidBlock) {
                        bws.getMatchContext()
                                /// This can be a [Map] for multiple types of fluids,
                                /// but this should be enough for now.
                                /// Using an [ArrayList] here since we need to sort this later.
                                /// [LinkedList] would be horrible for that
                                .getOrPut(FLUID_BLOCKS_KEY, new ArrayList<>())
                                .add(bws.getPos());
                        return true;
                    }
                    return false;
                },
                () -> new BlockInfo[] { new BlockInfo(Blocks.AIR) });
    }

    public static void fillFluid(MultiblockControllerBase multi, List<BlockPos> toFill, FluidStack fluidStack) {
        fillFluid(multi, toFill, fluidStack.getFluid());
    }

    public static void fillFluid(MultiblockControllerBase multi, List<BlockPos> toFill, Fluid fluid) {
        if (toFill.isEmpty()) return;

        // TODO: is it necessary for a multi to have a recipe logic for this?
        AbstractRecipeLogic recipeLogic = multi.getRecipeLogic();
        if (recipeLogic == null) return;

        IMultipleTankHandler fluidInputs = ((AbstractRecipeLogicAccessor) recipeLogic).inputTank();
        if (fluidInputs == null) return;

        FluidStack toDrain = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
        FluidStack drained = fluidInputs.drain(toDrain, false);
        if (drained == null || drained.amount == 0) return;

        if (drained.amount == Fluid.BUCKET_VOLUME) {
            World world = multi.getWorld();
            BlockPos pos = toFill.get(0);

            if (world.isBlockLoaded(pos) &&
                    (world.isAirBlock(pos) || world.getBlockState(pos).getBlock() == fluid.getBlock())) {
                world.setBlockState(pos, fluid.getBlock().getDefaultState(), BlockFlags.SEND_TO_CLIENTS);
                fluidInputs.drain(drained, true);
                toFill.remove(0);
            }
        }
    }
}
