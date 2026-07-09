package supercritical.api.registries;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import org.jspecify.annotations.NullMarked;
import supercritical.BuildConfig;

@NullMarked
public final class SCRegistries {
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(BuildConfig.MOD_ID);
}
