package supercritical.data;

import com.tterrag.registrate.providers.ProviderType;
import supercritical.api.registries.SCRegistries;
import supercritical.data.lang.SCLangHandler;
import supercritical.data.tags.SCBlockTagLoader;
import supercritical.data.tags.SCFluidTagLoader;
import supercritical.data.tags.SCItemTagLoader;

public final class SCDatagen {

    private SCDatagen() {}

    public static void init() {
        SCRegistries.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, SCBlockTagLoader::init);
        SCRegistries.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, SCItemTagLoader::init);
        SCRegistries.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, SCFluidTagLoader::init);
        SCRegistries.REGISTRATE.addDataGenerator(ProviderType.LANG, SCLangHandler::init);
    }
}
