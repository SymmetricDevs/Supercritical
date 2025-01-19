package gregicality.nuclear.api.util;

import gregicality.nuclear.GCYNValues;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GCYNUtility {

    @NotNull
    public static ResourceLocation gcynId(@NotNull String path) {
        return new ResourceLocation(GCYNValues.MODID, path);
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
