package io.github.symmetricdevs.supercritical.api.pattern

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate
import com.lowdragmc.lowdraglib.utils.BlockInfo
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction

/**
 * Supercritical-specific pattern predicates and helpers.
 */
object ScritPredicates {
    const val FLUID_TO_FILL: String = "FluidToFill"
    val MARKER = markerPredicate()

    fun fluidFill(fluidStack: FluidStack): TraceabilityPredicate {
        return fluidFill(fluidStack.fluid)
    }

    fun fluidFill(fluid: Fluid): TraceabilityPredicate {
        // Match the fluid's block so BOTH source and flowing states satisfy the predicate.
        // Predicates.fluids(fluid) only matches the source fluid (e.g. Fluids.WATER); once a
        // placed source flows into a neighbouring 'W' cell it becomes Fluids.FLOWING_WATER, which
        // would fail the predicate and invalidate the structure. Source/flowing share the same
        // Block (Blocks.WATER), so match by block — same as legacy's blockState.getBlock()==fluidBlock.
        val fluidBlock = fluid.defaultFluidState().createLegacyBlock().block
        return Predicates.blocks(fluidBlock).or(MARKER)
    }

    fun fillFluid(multi: MultiblockControllerMachine, toFill: MutableList<BlockPos>, fluidStack: FluidStack) {
        fillFluid(multi, toFill, fluidStack.fluid)
    }

    fun fillFluid(multi: MultiblockControllerMachine, toFill: MutableList<BlockPos>, fluid: Fluid) {
        if (toFill.isEmpty()) return

        val world = multi.level
        if (world == null || world.isClientSide) return
        if (multi !is IRecipeCapabilityHolder) return

        val inputTanks = multi.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP) + multi.getCapabilitiesFlat(
            IO.BOTH,
            FluidRecipeCapability.CAP
        )
        val toDrain = FluidStack(fluid, FluidType.BUCKET_VOLUME)
        val fluidBlock = fluid.defaultFluidState().createLegacyBlock().block
        for (handler in inputTanks) {
            // Import hatches (IO.IN) refuse IFluidHandler.drain — canCapOutput() is false, so drain
            // returns EMPTY and the pool could never extract water. drainInternal bypasses the IO
            // gate and actually consumes from the import hatch, matching legacy's
            // recipeLogic.inputTank.drain(...).
            val tank = handler as? NotifiableFluidTank ?: continue
            val drained = tank.drainInternal(toDrain, FluidAction.SIMULATE)
            if (drained.amount == FluidType.BUCKET_VOLUME) {
                tank.drainInternal(toDrain, FluidAction.EXECUTE)
                val pos = toFill.last()
                // Place a source if the cell is air OR already any water block (source/flowing),
                // so flowing water spilled from a neighbour is converted to a source, not skipped.
                if (world.isLoaded(pos) &&
                    (world.isEmptyBlock(pos) || world.getBlockState(pos).block == fluidBlock)
                ) {
                    world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL)
                }
                toFill.removeLast()
                return
            }
        }
    }

    private fun markerPredicate(): TraceabilityPredicate = Predicates.custom({ bws ->
        Predicates.air().test(bws) && bws.matchContext
            .getOrPut(FLUID_TO_FILL, ArrayDeque<BlockPos>())
            // This should always return true
            .add(bws.pos)
    }, { arrayOf(BlockInfo(Blocks.AIR)) })
}
