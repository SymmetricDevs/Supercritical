package supercritical.api.nuclear.fission;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class ModeratorRegistry {

    private static final Map<ModeratorInfo, IModeratorStats> MODERATORS = new Object2ObjectOpenHashMap<>();

    public static void registerModerator(@NotNull ItemStack stack, @NotNull IModeratorStats moderator) {
        ItemBlock block = ((ItemBlock) stack.getItem());
        ResourceLocation registry = block.getBlock().getRegistryName();
        int meta = block.getMetadata(stack);
        MODERATORS.put(new ModeratorInfo(registry, meta), moderator);
    }

    public static void registerModerator(@NotNull IBlockState state, @NotNull IModeratorStats moderator) {
        ResourceLocation registry = state.getBlock().getRegistryName();
        int meta = state.getBlock().getMetaFromState(state);
        MODERATORS.put(new ModeratorInfo(registry, meta), moderator);
    }

    @Nullable
    public static IModeratorStats getModerator(@NotNull IBlockState state) {
        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);
        return MODERATORS.get(new ModeratorInfo(block.getRegistryName(), meta));
    }

    public static Collection<ModeratorInfo> getAllModerators() {
        return MODERATORS.keySet();
    }

    public static class ModeratorInfo {
        @Getter
        private final ResourceLocation registryName;
        @Getter
        private final int meta;

        private ModeratorInfo(ResourceLocation registryName, int meta) {
            this.registryName = registryName;
            this.meta = meta;
        }

        @Override
        public int hashCode() {
            return registryName.hashCode() + meta * 1000;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ModeratorInfo && ((ModeratorInfo) obj).registryName.equals(registryName) && ((ModeratorInfo) obj).meta == meta;
        }
    }
}
