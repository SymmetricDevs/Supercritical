package supercritical.common.registry

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import supercritical.BuildConfig

object ScritRegistration {
    val REGISTRATE: GTRegistrate = GTRegistrate.create(BuildConfig.MOD_ID)
}