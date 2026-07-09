package supercritical.api.util

import net.minecraft.resources.ResourceLocation
import supercritical.BuildConfig

object SCUtility {
    fun scId(path: String): ResourceLocation {
        return BuildConfig.TEMPLATE_RL.withPath(path)
    }

    fun replace(s: String, index: Int, c: Char): String {
        return s.substring(0, index) + c + s.substring(index + 1)
    }
}
