package supercritical.api.nuclear.fission

import com.gregtechceu.gtceu.utils.ItemStackHashStrategy
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap
import net.minecraft.world.item.ItemStack
import java.util.*

object FissionFuelRegistry {
    private val IDENTIFIED_FUELS: MutableMap<String?, IFissionFuelStats?> = linkedMapOf()
    private val FUELS: MutableMap<ItemStack, IFissionFuelStats?> = Object2ObjectOpenCustomHashMap(ItemStackHashStrategy.comparingAllButCount())

    fun registerFuel(item: ItemStack, fuel: IFissionFuelStats) {
        IDENTIFIED_FUELS[fuel.id] = fuel
        if (!item.isEmpty) {
            FUELS[item.copyWithCount(1)] = fuel
        }
    }

    fun registerFuel(fuel: IFissionFuelStats) {
        IDENTIFIED_FUELS[fuel.id] = fuel
    }

    fun getFissionFuel(stack: ItemStack): IFissionFuelStats? {
        if (stack.isEmpty) return null
        return FUELS[stack]
    }

    val allFissionableRods: MutableCollection<ItemStack>
        get() = Collections.unmodifiableSet<ItemStack>(FUELS.keys)

    fun getFissionFuel(name: String?): IFissionFuelStats? = IDENTIFIED_FUELS[name]

    val allFuelStats: MutableCollection<IFissionFuelStats?>
        get() = Collections.unmodifiableCollection<IFissionFuelStats?>(IDENTIFIED_FUELS.values)
}
