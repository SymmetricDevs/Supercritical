package supercritical.api.nuclear.fission;

import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FissionFuelRegistry {

    private static final Map<String, IFissionFuelStats> IDENTIFIED_FUELS = new LinkedHashMap<>();
    private static final Map<ItemStack, IFissionFuelStats> FUELS = new LinkedHashMap<>();

    private FissionFuelRegistry() {}

    public static void registerFuel(ItemStack item, IFissionFuelStats fuel) {
        IDENTIFIED_FUELS.put(fuel.getId(), fuel);
        if (!item.isEmpty()) {
            FUELS.put(item.copyWithCount(1), fuel);
        }
    }

    public static void registerFuel(IFissionFuelStats fuel) {
        IDENTIFIED_FUELS.put(fuel.getId(), fuel);
    }

    public static IFissionFuelStats getFissionFuel(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (var entry : FUELS.entrySet()) {
            if (ItemStack.isSameItemSameTags(entry.getKey(), stack)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static Collection<ItemStack> getAllFissionableRods() {
        return Collections.unmodifiableSet(FUELS.keySet());
    }

    public static IFissionFuelStats getFissionFuel(String name) {
        return IDENTIFIED_FUELS.get(name);
    }

    public static Collection<IFissionFuelStats> getAllFuelStats() {
        return Collections.unmodifiableCollection(IDENTIFIED_FUELS.values());
    }
}
