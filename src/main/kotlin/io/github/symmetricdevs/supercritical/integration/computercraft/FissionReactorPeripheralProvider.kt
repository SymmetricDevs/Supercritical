package io.github.symmetricdevs.supercritical.integration.computercraft

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.api.peripheral.IPeripheralProvider
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraftforge.common.util.LazyOptional
import io.github.symmetricdevs.supercritical.common.machine.multiblock.fission.FissionReactor

/**
 * Global CC:Tweaked peripheral provider for the fission reactor controller. Registered once via
 * [ComputerCraftIntegration]; ComputerCraft queries it for every block a modem tries to wrap. When
 * the block entity is a GTCEu machine whose [IMachineBlockEntity.getMetaMachine] is a
 * [FissionReactor], it returns a [FissionReactorPeripheral]; otherwise an empty
 * optional so other providers (and other blocks) are unaffected.
 */
object FissionReactorPeripheralProvider : IPeripheralProvider {

    override fun getPeripheral(level: Level, pos: BlockPos, side: Direction): LazyOptional<IPeripheral> {
        val holder = level.getBlockEntity(pos) as? IMachineBlockEntity ?: return LazyOptional.empty()
        val machine = holder.metaMachine
        return if (machine is FissionReactor) {
            LazyOptional.of { FissionReactorPeripheral(machine) }
        } else {
            LazyOptional.empty()
        }
    }
}
