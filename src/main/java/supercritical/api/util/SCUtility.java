package supercritical.api.util;

import net.minecraft.resources.ResourceLocation;
import supercritical.BuildConfig;

public final class SCUtility {

    private SCUtility() {}

    public static ResourceLocation scId(String path) {
        return BuildConfig.TEMPLATE_RL.withPath(path);
    }

    public static String replace(String s, int index, char c) {
        return s.substring(0, index) + c + s.substring(index + 1);
    }
}
