package supercritical.api.registries

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import org.jspecify.annotations.NullMarked
import supercritical.BuildConfig

@NullMarked
object SCRegistries {
    val REGISTRATE: GTRegistrate = GTRegistrate.create(BuildConfig.MOD_ID)
}
