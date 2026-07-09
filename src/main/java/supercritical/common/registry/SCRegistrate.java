package supercritical.common.registry;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import supercritical.api.registry.SCRegistries;

/**
 * Compatibility access for older port code. New GTCEu-addon bootstrap code should use {@link SCRegistries}.
 */
public final class SCRegistrate {

    public static final GTRegistrate REGISTRATE = SCRegistries.REGISTRATE;

    private SCRegistrate() {}
}
