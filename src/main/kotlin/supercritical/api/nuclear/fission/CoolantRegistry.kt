package supercritical.api.nuclear.fission

import net.minecraft.world.level.material.Fluid
import java.util.*

object CoolantRegistry {
    private val COOLANTS: MutableMap<Fluid?, ICoolantStats?> = LinkedHashMap<Fluid?, ICoolantStats?>()
    private val COOLANTS_INVERSE: MutableMap<ICoolantStats?, Fluid?> = LinkedHashMap<ICoolantStats?, Fluid?>()

    fun registerCoolant(fluid: Fluid?, coolant: ICoolantStats?) {
        COOLANTS.put(fluid, coolant)
        COOLANTS_INVERSE.put(coolant, fluid)
    }

    fun getCoolant(fluid: Fluid?): ICoolantStats? {
        return COOLANTS.get(fluid)
    }

    val allCoolants: MutableCollection<Fluid?>
        get() = Collections.unmodifiableSet<Fluid?>(COOLANTS.keys)

    fun originalFluid(stats: ICoolantStats?): Fluid? {
        return COOLANTS_INVERSE.get(stats)
    }

    val allCoolantStats: MutableCollection<ICoolantStats?>
        get() = Collections.unmodifiableCollection<ICoolantStats?>(COOLANTS.values)
}
