package supercritical.api.util;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import lombok.experimental.UtilityClass;
import supercritical.SCValues;

@UtilityClass
public class SCUtility {

    @NotNull
    public static ResourceLocation scId(@NotNull String path) {
        return new ResourceLocation(SCValues.MODID, path);
    }

    public static String replace(String str, int index, char replace) {
        if (str == null) {
            return null;
        } else if (index < 0 || index >= str.length()) {
            return str;
        }
        char[] chars = str.toCharArray();
        chars[index] = replace;
        return String.valueOf(chars);
    }
}
