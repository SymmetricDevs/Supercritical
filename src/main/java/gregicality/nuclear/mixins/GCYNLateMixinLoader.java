package gregicality.nuclear.mixins;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.List;
import java.util.stream.Collectors;

public class GCYNLateMixinLoader implements ILateMixinLoader {

    public static final List<String> modMixins = ImmutableList.of("gregtech");

    public static boolean shouldEnableModMixin(String mod) {
        return Loader.isModLoaded(mod);
    }

    @Override
    public List<String> getMixinConfigs() {
        return modMixins.stream().map(mod -> "mixins.gcyn." + mod + ".json").collect(Collectors.toList());
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        String[] parts = mixinConfig.split("\\.");
        return parts.length != 4 || shouldEnableModMixin(parts[2]);
    }
}
