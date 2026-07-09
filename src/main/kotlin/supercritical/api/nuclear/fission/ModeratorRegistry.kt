package supercritical.api.nuclear.fission

import net.minecraft.world.level.block.Block
import java.util.*

object ModeratorRegistry {
    private val MODERATORS: MutableMap<Block?, IModeratorStats?> = LinkedHashMap<Block?, IModeratorStats?>()

    fun registerModerator(block: Block?, stats: IModeratorStats?) {
        MODERATORS.put(block, stats)
    }

    fun getModerator(block: Block?): IModeratorStats? {
        return MODERATORS.get(block)
    }

    val allModerators: MutableCollection<Block?>
        get() = Collections.unmodifiableSet<Block?>(MODERATORS.keys)

    val allModeratorStats: MutableCollection<IModeratorStats?>
        get() = Collections.unmodifiableCollection<IModeratorStats?>(MODERATORS.values)
}
