package supercritical.api.registry;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import supercritical.SCValues;

public final class SCRegistries {

    public static final GTRegistrate REGISTRATE = GTRegistrate.create(SCValues.MODID);

    private SCRegistries() {}
}
