package supercritical.api.nuclear.fission

import net.minecraft.world.item.ItemStack
import java.util.*

object FissionFuelRegistry {
    private val IDENTIFIED_FUELS: MutableMap<String?, IFissionFuelStats?> = LinkedHashMap<String?, IFissionFuelStats?>()
    private val FUELS: MutableMap<ItemStack?, IFissionFuelStats?> = LinkedHashMap<ItemStack?, IFissionFuelStats?>()

    fun registerFuel(item: ItemStack, fuel: IFissionFuelStats) {
        IDENTIFIED_FUELS.put(fuel.getId(), fuel)
        if (!item.isEmpty()) {
            FUELS.put(item.copyWithCount(1), fuel)
        }
    }

    fun registerFuel(fuel: IFissionFuelStats) {
        IDENTIFIED_FUELS.put(fuel.getId(), fuel)
    }

    fun getFissionFuel(stack: ItemStack): IFissionFuelStats? {
        if (stack.isEmpty()) return null
        for (entry in FUELS.entries) {
            if (ItemStack.isSameItemSameTags(entry.key, stack)) {
                return entry.value
            }
        }
        return null
    }

    val allFissionableRods: MutableCollection<ItemStack?>
        get() = Collections.unmodifiableSet<ItemStack?>(FUELS.keys)

    fun getFissionFuel(name: String?): IFissionFuelStats? {
        return IDENTIFIED_FUELS.get(name)
    }

    val allFuelStats: MutableCollection<IFissionFuelStats?>
        get() = Collections.unmodifiableCollection<IFissionFuelStats?>(IDENTIFIED_FUELS.values)
}
