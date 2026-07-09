package supercritical.api.pattern

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.pattern.MultiblockState
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate
import com.lowdragmc.lowdraglib.utils.BlockInfo
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * Supercritical-specific pattern predicates and helpers.
 */
object SCPredicates {
    const val FLUID_BLOCKS_KEY: String = "FluidBlocks"

    fun fluid(fluidStack: FluidStack): TraceabilityPredicate {
        return fluid(fluidStack.getFluid())
    }

    fun fluid(fluid: Fluid): TraceabilityPredicate {
        val fluidBlock = fluid.defaultFluidState().createLegacyBlock().getBlock()
        val stillState = fluidBlock.defaultBlockState()

        return TraceabilityPredicate(
            Predicate { state: MultiblockState? ->
                val blockState = state!!.getBlockState()
                if (blockState === stillState) return@Predicate true
                val world = state.getWorld()
                val pos = state.getPos()
                if (world.isEmptyBlock(pos) || blockState.getBlock() === fluidBlock) {
                    state.getMatchContext()
                        .getOrPut<ArrayList<Any?>?>(FLUID_BLOCKS_KEY, ArrayList<Any?>())
                        .add(pos)
                    return@Predicate true
                }
                false
            },
            Supplier { arrayOf<BlockInfo>(BlockInfo.fromBlockState(stillState)) })
    }

    fun fillFluid(multi: MetaMachine, toFill: MutableList<BlockPos>, fluidStack: FluidStack) {
        fillFluid(multi, toFill, fluidStack.getFluid())
    }

    fun fillFluid(multi: MetaMachine, toFill: MutableList<BlockPos>, fluid: Fluid) {
        if (toFill.isEmpty()) return

        val world = multi.getLevel()
        if (world == null || world.isClientSide) return
        if (multi !is IRecipeCapabilityHolder) return

        val inputTanks: MutableList<IRecipeHandler<*>?> = ArrayList<IRecipeHandler<*>?>()
        inputTanks.addAll(multi.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP))
        inputTanks.addAll(multi.getCapabilitiesFlat(IO.BOTH, FluidRecipeCapability.CAP))

        val toDrain = FluidStack(fluid, FluidType.BUCKET_VOLUME)
        for (handler in inputTanks) {
            if (handler !is IFluidHandler) continue
            val drained = handler.drain(toDrain, FluidAction.SIMULATE)
            if (drained.getAmount() == FluidType.BUCKET_VOLUME) {
                handler.drain(toDrain, FluidAction.EXECUTE)
                val pos = toFill.get(0)
                if (world.isLoaded(pos) &&
                    (world.isEmptyBlock(pos) || world.getBlockState(pos).getFluidState().getType() === fluid)
                ) {
                    world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL)
                }
                toFill.removeAt(0)
                return
            }
        }
    }
}
