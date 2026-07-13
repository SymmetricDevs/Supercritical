package io.github.symmetricdevs.supercritical.api.machine.multiblock

import net.minecraft.core.BlockPos
import io.github.symmetricdevs.supercritical.common.machine.multiblock.fission.FissionReactor

interface IFissionReactorHatch {
    /**
     * @param depth The depth of the reactor that needs checking
     * @return If the channel directly below the hatch is valid or not
     */
    fun checkValidity(depth: Int): Boolean

    val hatchPos: BlockPos?

    fun getController(): FissionReactor? = null

    fun isLocked(): Boolean = false
}
