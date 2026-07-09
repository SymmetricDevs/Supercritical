package supercritical.api.nuclear.fission;

import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModeratorRegistry {

    private static final Map<Block, IModeratorStats> MODERATORS = new LinkedHashMap<>();

    private ModeratorRegistry() {}

    public static void registerModerator(Block block, IModeratorStats stats) {
        MODERATORS.put(block, stats);
    }

    public static IModeratorStats getModerator(Block block) {
        return MODERATORS.get(block);
    }

    public static Collection<Block> getAllModerators() {
        return Collections.unmodifiableSet(MODERATORS.keySet());
    }

    public static Collection<IModeratorStats> getAllModeratorStats() {
        return Collections.unmodifiableCollection(MODERATORS.values());
    }
}
