package supercritical.api.metatileentity.multiblock

import net.minecraft.core.BlockPos
import supercritical.common.metatileentities.multi.MetaTileEntityFissionReactor

interface IFissionReactorHatch {
    /**
     * @param depth The depth of the reactor that needs checking
     * @return If the channel directly below the hatch is valid or not
     */
    fun checkValidity(depth: Int): Boolean

    fun canContinue(depletion: Double): Boolean {
        return true
    }

    val pos: BlockPos?

    /**
     * Called by the controller when it forms so the hatch can store a weak reference.
     */
    fun setController(controller: MetaTileEntityFissionReactor?) {}

    fun getController(): MetaTileEntityFissionReactor? {
        return null
    }

    /**
     * @return the stored controller reference, or null if none is stored.
     */
    fun hasController(): Boolean {
        return getController() != null
    }

    fun isLocked(): Boolean {
        return false
    }

    fun setLocked(locked: Boolean) {}
}
