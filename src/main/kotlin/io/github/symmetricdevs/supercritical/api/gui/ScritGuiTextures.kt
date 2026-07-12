package io.github.symmetricdevs.supercritical.api.gui

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture
import io.github.symmetricdevs.supercritical.util.scId

/**
 * Supercritical GUI textures, shipped under this mod's own `supercritical` resource namespace
 * (`assets/supercritical/textures/gui/...`).
 */
object ScritGuiTextures {
    val BUTTON_CONTROL_ROD_HELPER: ResourceTexture = scRl("widget/button_control_rod_helper.png")
    val DARK_SLIDER_BACKGROUND: ResourceTexture = scRl("widget/dark_slider_background.png")
    val DARK_SLIDER_ICON: ResourceTexture = scRl("widget/dark_slider.png")
    val PROGRESS_BAR_FISSION_HEAT: ResourceTexture = scRl("progress_bar/progress_bar_fission_heat.png")
    val PROGRESS_BAR_FISSION_PRESSURE: ResourceTexture = scRl("progress_bar/progress_bar_fission_pressure.png")
    val PROGRESS_BAR_FISSION_ENERGY: ResourceTexture = scRl("progress_bar/progress_bar_fission_energy.png")

    private fun scRl(subPath: String) = ResourceTexture(scId("textures/gui/$subPath"))
}
