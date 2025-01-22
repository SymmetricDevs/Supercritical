package supercritical.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockGasCentrifugeCasing extends VariantBlock<BlockGasCentrifugeCasing.GasCentrifugeCasingType> {

    public BlockGasCentrifugeCasing() {
        super(Material.IRON);
        setTranslationKey("gas_centrifuge_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(GasCentrifugeCasingType.GAS_CENTRIFUGE_COLUMN));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return state != getState(GasCentrifugeCasingType.GAS_CENTRIFUGE_COLUMN);
    }

    public enum GasCentrifugeCasingType implements IStringSerializable, IStateHarvestLevel {

        GAS_CENTRIFUGE_COLUMN("gas_centrifuge_column", 2);

        private final String name;
        private final int harvestLevel;

        GasCentrifugeCasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }
    }
}
