package supercritical.api.util;

import net.minecraft.resources.ResourceLocation;
import supercritical.SCValues;

public final class SCUtility {

    private SCUtility() {}

    public static ResourceLocation scId(String path) {
        return new ResourceLocation(SCValues.MODID, path);
    }

    public static String replace(String s, int index, char c) {
        return s.substring(0, index) + c + s.substring(index + 1);
    }
}
