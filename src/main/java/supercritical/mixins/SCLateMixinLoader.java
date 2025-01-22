package supercritical.mixins;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraftforge.fml.common.Loader;

import com.google.common.collect.ImmutableList;

import zone.rong.mixinbooter.ILateMixinLoader;

public class SCLateMixinLoader implements ILateMixinLoader {

    public static final List<String> modMixins = ImmutableList.of("gregtech");

    public static boolean shouldEnableModMixin(String mod) {
        return Loader.isModLoaded(mod);
    }

    @Override
    public List<String> getMixinConfigs() {
        return modMixins.stream().map(mod -> "mixins.supercritical." + mod + ".json").collect(Collectors.toList());
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        String[] parts = mixinConfig.split("\\.");
        return parts.length != 4 || shouldEnableModMixin(parts[2]);
    }
}
