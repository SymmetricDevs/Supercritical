package supercritical.api.nuclear.fission;

import net.minecraft.world.level.material.Fluid;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CoolantRegistry {

    private static final Map<Fluid, ICoolantStats> COOLANTS = new LinkedHashMap<>();
    private static final Map<ICoolantStats, Fluid> COOLANTS_INVERSE = new LinkedHashMap<>();

    private CoolantRegistry() {}

    public static void registerCoolant(Fluid fluid, ICoolantStats coolant) {
        COOLANTS.put(fluid, coolant);
        COOLANTS_INVERSE.put(coolant, fluid);
    }

    public static ICoolantStats getCoolant(Fluid fluid) {
        return COOLANTS.get(fluid);
    }

    public static Collection<Fluid> getAllCoolants() {
        return Collections.unmodifiableSet(COOLANTS.keySet());
    }

    public static Fluid originalFluid(ICoolantStats stats) {
        return COOLANTS_INVERSE.get(stats);
    }

    public static Collection<ICoolantStats> getAllCoolantStats() {
        return Collections.unmodifiableCollection(COOLANTS.values());
    }
}
