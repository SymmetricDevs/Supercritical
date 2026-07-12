package io.github.symmetricdevs.supercritical.api.machine.multiblock

import net.minecraft.core.BlockPos
import io.github.symmetricdevs.supercritical.common.machine.multiblock.fission.FissionReactor

interface IFissionReactorHatch {
    /**
     * @param depth The depth of the reactor that needs checking
     * @return If the channel directly below the hatch is valid or not
     */
    fun checkValidity(depth: Int): Boolean

    fun canContinue(depletion: Double): Boolean = true

    val hatchPos: BlockPos?

    /**
     * Called by the controller when it forms so the hatch can store a weak reference.
     */
    fun setController(controller: FissionReactor?) {}

    fun getController(): FissionReactor? = null

    /**
     * @return the stored controller reference, or null if none is stored.
     */
    fun hasController(): Boolean = getController() != null

    fun isLocked(): Boolean = false
}
