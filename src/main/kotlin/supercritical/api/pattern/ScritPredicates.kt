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
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import supercritical.common.ScritConfig

/**
 * Supercritical-specific pattern predicates and helpers.
 */
object ScritPredicates {
    const val FLUID_BLOCKS_KEY: String = "FluidBlocks"

    fun fluid(fluidStack: FluidStack): TraceabilityPredicate {
        return fluid(fluidStack.fluid)
    }

    fun fluid(fluid: Fluid): TraceabilityPredicate {
        val fluidBlock = fluid.defaultFluidState().createLegacyBlock().block
        val stillState = fluidBlock.defaultBlockState()

        return TraceabilityPredicate(
            { state: MultiblockState ->
                val blockState = state.blockState
                if (blockState === stillState) return@TraceabilityPredicate true
                val world = state.getWorld()
                val pos = state.pos
                if (world.isEmptyBlock(pos) || blockState.block === fluidBlock) {
                    state.matchContext
                        .getOrPut(FLUID_BLOCKS_KEY, arrayListOf<Any?>())
                        .add(pos)
                    return@TraceabilityPredicate true
                }
                false
            }
        ) {
            // Legacy gated the previewed fluid block on this config (default false -> AIR/hidden).
            val showFluids = ScritConfig.INSTANCE.misc.showFluidsForAutoFillingMultiblocks
            arrayOf(BlockInfo.fromBlockState(if (showFluids) stillState else Blocks.AIR.defaultBlockState()))
        }
    }

    fun fillFluid(multi: MetaMachine, toFill: MutableList<BlockPos>, fluidStack: FluidStack) {
        fillFluid(multi, toFill, fluidStack.fluid)
    }

    fun fillFluid(multi: MetaMachine, toFill: MutableList<BlockPos>, fluid: Fluid) {
        if (toFill.isEmpty()) return

        val world = multi.level
        if (world == null || world.isClientSide) return
        if (multi !is IRecipeCapabilityHolder) return

        val inputTanks = buildList<IRecipeHandler<*>?> {
            addAll(multi.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP))
            addAll(multi.getCapabilitiesFlat(IO.BOTH, FluidRecipeCapability.CAP))
        }

        val toDrain = FluidStack(fluid, FluidType.BUCKET_VOLUME)
        for (handler in inputTanks) {
            if (handler !is IFluidHandler) continue
            val drained = handler.drain(toDrain, FluidAction.SIMULATE)
            if (drained.amount == FluidType.BUCKET_VOLUME) {
                handler.drain(toDrain, FluidAction.EXECUTE)
                val pos = toFill[0]
                if (world.isLoaded(pos) &&
                    (world.isEmptyBlock(pos) || world.getBlockState(pos).fluidState.type == fluid)
                ) {
                    world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL)
                }
                toFill.removeAt(0)
                return
            }
        }
    }
}
