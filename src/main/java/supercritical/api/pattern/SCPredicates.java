package supercritical.api.pattern;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Supercritical-specific pattern predicates and helpers.
 */
public class SCPredicates {

    public static final String FLUID_BLOCKS_KEY = "FluidBlocks";

    public static TraceabilityPredicate fluid(FluidStack fluidStack) {
        return fluid(fluidStack.getFluid());
    }

    public static TraceabilityPredicate fluid(Fluid fluid) {
        Block fluidBlock = fluid.defaultFluidState().createLegacyBlock().getBlock();
        BlockState stillState = fluidBlock.defaultBlockState();

        return new TraceabilityPredicate(
                state -> {
                    BlockState blockState = state.getBlockState();
                    if (blockState == stillState) return true;
                    Level world = state.getWorld();
                    BlockPos pos = state.getPos();
                    if (world.isEmptyBlock(pos) || blockState.getBlock() == fluidBlock) {
                        state.getMatchContext()
                                .getOrPut(FLUID_BLOCKS_KEY, new ArrayList<>())
                                .add(pos);
                        return true;
                    }
                    return false;
                },
                () -> new BlockInfo[] { BlockInfo.fromBlockState(stillState) });
    }

    public static void fillFluid(MetaMachine multi, List<BlockPos> toFill, FluidStack fluidStack) {
        fillFluid(multi, toFill, fluidStack.getFluid());
    }

    public static void fillFluid(MetaMachine multi, List<BlockPos> toFill, Fluid fluid) {
        if (toFill.isEmpty()) return;

        Level world = multi.getLevel();
        if (world == null || world.isClientSide) return;
        if (!(multi instanceof IRecipeCapabilityHolder holder)) return;

        List<com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler<?>> inputTanks = new ArrayList<>();
        inputTanks.addAll(holder.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP));
        inputTanks.addAll(holder.getCapabilitiesFlat(IO.BOTH, FluidRecipeCapability.CAP));

        FluidStack toDrain = new FluidStack(fluid, FluidType.BUCKET_VOLUME);
        for (var handler : inputTanks) {
            if (!(handler instanceof IFluidHandler fluidHandler)) continue;
            FluidStack drained = fluidHandler.drain(toDrain, IFluidHandler.FluidAction.SIMULATE);
            if (drained.getAmount() == FluidType.BUCKET_VOLUME) {
                fluidHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                BlockPos pos = toFill.get(0);
                if (world.isLoaded(pos) &&
                        (world.isEmptyBlock(pos) || world.getBlockState(pos).getFluidState().getType() == fluid)) {
                    world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL);
                }
                toFill.remove(0);
                return;
            }
        }
    }
}
