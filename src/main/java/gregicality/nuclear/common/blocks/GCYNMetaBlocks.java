package gregicality.nuclear.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import gregtech.common.blocks.MetaBlocks;

public class GCYNMetaBlocks {

    // Nuclear Blocks
    public static BlockFissionCasing FISSION_CASING;
    public static BlockNuclearCasing NUCLEAR_CASING;
    public static BlockGasCentrifugeCasing GAS_CENTRIFUGE_CASING;
    public static BlockPanelling PANELLING;

    public static void init() {
        FISSION_CASING = new BlockFissionCasing();
        FISSION_CASING.setRegistryName("fission_casing");
        NUCLEAR_CASING = new BlockNuclearCasing();
        NUCLEAR_CASING.setRegistryName("nuclear_casing");
        GAS_CENTRIFUGE_CASING = new BlockGasCentrifugeCasing();
        GAS_CENTRIFUGE_CASING.setRegistryName("gas_centrifuge_casing");
        PANELLING = new BlockPanelling();
        PANELLING.setRegistryName("panelling");
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        registerItemModel(FISSION_CASING);
        registerItemModel(GAS_CENTRIFUGE_CASING);
        registerItemModel(PANELLING);
        NUCLEAR_CASING.onModelRegister();
    }

    @SuppressWarnings("DataFlowIssue")
    @SideOnly(Side.CLIENT)
    private static void registerItemModel(@NotNull Block block) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            MetaBlocks.statePropertiesToString(state.getProperties())));
        }
    }
}
