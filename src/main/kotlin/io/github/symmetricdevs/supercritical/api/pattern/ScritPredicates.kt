package io.github.symmetricdevs.supercritical.api.pattern

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine
import com.gregtechceu.gtceu.api.pattern.Predicates
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

/**
 * Supercritical-specific pattern predicates and helpers.
 */
object ScritPredicates {
    const val FLUID_TO_FILL: String = "FluidToFill"
    val MARKER = markerPredicate()

    fun fluidFill(fluidStack: FluidStack): TraceabilityPredicate {
        return fluidFill(fluidStack.fluid)
    }

    fun fluidFill(fluid: Fluid): TraceabilityPredicate = Predicates.fluids(fluid).or(MARKER)

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
        for (handler in inputTanks) {
            if (handler !is IFluidHandler) continue
            val drained = handler.drain(toDrain, FluidAction.SIMULATE)
            if (drained.amount == FluidType.BUCKET_VOLUME) {
                handler.drain(toDrain, FluidAction.EXECUTE)
                val pos = toFill.first()
                if (world.isLoaded(pos) &&
                    (world.isEmptyBlock(pos) || world.getBlockState(pos).fluidState.type == fluid)
                ) {
                    world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL)
                }
                toFill.removeFirst()
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
