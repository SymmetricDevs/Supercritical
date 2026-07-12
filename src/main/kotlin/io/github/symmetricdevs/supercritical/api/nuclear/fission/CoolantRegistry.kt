package io.github.symmetricdevs.supercritical.api.nuclear.fission

import net.minecraft.world.level.material.Fluid
import java.util.*

object CoolantRegistry {
    private val COOLANTS: MutableMap<Fluid?, ICoolantStats?> = linkedMapOf()
    private val COOLANTS_INVERSE: MutableMap<ICoolantStats?, Fluid?> = linkedMapOf()

    fun registerCoolant(fluid: Fluid?, coolant: ICoolantStats?) {
        COOLANTS[fluid] = coolant
        COOLANTS_INVERSE[coolant] = fluid
    }

    fun getCoolant(fluid: Fluid?): ICoolantStats? = COOLANTS[fluid]

    val allCoolants: MutableCollection<Fluid?>
        get() = Collections.unmodifiableSet<Fluid?>(COOLANTS.keys)

    fun originalFluid(stats: ICoolantStats?): Fluid? = COOLANTS_INVERSE[stats]

    val allCoolantStats: MutableCollection<ICoolantStats?>
        get() = Collections.unmodifiableCollection<ICoolantStats?>(COOLANTS.values)
}
