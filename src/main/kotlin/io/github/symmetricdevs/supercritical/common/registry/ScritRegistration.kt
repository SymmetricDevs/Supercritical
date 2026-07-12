package io.github.symmetricdevs.supercritical.common.registry

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import io.github.symmetricdevs.supercritical.BuildConfig

object ScritRegistration {
    val REGISTRATE: GTRegistrate = GTRegistrate.create(BuildConfig.MOD_ID)
}