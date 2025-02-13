package supercritical.api.pattern;

import java.util.*;
import java.util.function.Supplier;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.common.blocks.MetaBlocks;
import supercritical.mixins.gregtech.MultiblockShapeInfoBuilderAccessor;

/**
 * Extends {@link MultiblockShapeInfo.Builder} to incorporate directional awareness.
 */
public class DirectionalShapeInfoBuilder extends MultiblockShapeInfo.Builder {

    protected final MultiblockShapeInfoBuilderAccessor self = (MultiblockShapeInfoBuilderAccessor) this;

    private final RelativeDirection[] structureDir = new RelativeDirection[3];

    /**
     * @param structureDir The directions that the provided block pattern is based upon (character, string, row).
     */
    public DirectionalShapeInfoBuilder(@NotNull RelativeDirection... structureDir) {
        this(structureDir[0], structureDir[1], structureDir[2]);
    }

    public DirectionalShapeInfoBuilder(@NotNull RelativeDirection one, @NotNull RelativeDirection two,
                                       @NotNull RelativeDirection three) {
        this.structureDir[0] = Objects.requireNonNull(one);
        this.structureDir[1] = Objects.requireNonNull(two);
        this.structureDir[2] = Objects.requireNonNull(three);
        int flags = 0;
        for (RelativeDirection relativeDirection : this.structureDir) {
            switch (relativeDirection) {
                case UP, DOWN -> flags |= 0x1;
                case LEFT, RIGHT -> flags |= 0x2;
                case FRONT, BACK -> flags |= 0x4;
            }
        }
        if (flags != 0x7) throw new IllegalArgumentException("The directions must be on different axes!");
    }

    @Override
    public DirectionalShapeInfoBuilder aisle(String... data) {
        super.aisle(data);
        return this;
    }

    @Override
    public DirectionalShapeInfoBuilder where(char symbol, BlockInfo value) {
        super.where(symbol, value);
        return this;
    }

    @Override
    public DirectionalShapeInfoBuilder where(char symbol, IBlockState blockState) {
        return where(symbol, new BlockInfo(blockState));
    }

    @Override
    public DirectionalShapeInfoBuilder where(char symbol, IBlockState blockState, TileEntity tileEntity) {
        return where(symbol, new BlockInfo(blockState, tileEntity));
    }

    @Override
    public DirectionalShapeInfoBuilder where(char symbol, MetaTileEntity tileEntity, EnumFacing frontSide) {
        MetaTileEntityHolder holder = new MetaTileEntityHolder();
        holder.setMetaTileEntity(tileEntity);
        holder.getMetaTileEntity().onPlacement();
        holder.getMetaTileEntity().setFrontFacing(frontSide);
        return where(symbol, new BlockInfo(MetaBlocks.MACHINE.getDefaultState(), holder));
    }

    /**
     * @param partSupplier Should supply either a MetaTileEntity or an IBlockState.
     */
    @Override
    public DirectionalShapeInfoBuilder where(char symbol, Supplier<?> partSupplier, EnumFacing frontSideIfTE) {
        Object part = partSupplier.get();
        if (part instanceof IBlockState) {
            return where(symbol, (IBlockState) part);
        } else if (part instanceof MetaTileEntity) {
            return where(symbol, (MetaTileEntity) part, frontSideIfTE);
        } else throw new IllegalArgumentException(
                "Supplier must supply either a MetaTileEntity or an IBlockState! Actual: " + part.getClass());
    }

    @NotNull
    private BlockInfo[][][] bakeArray() {
        final int maxZ = self.getShape().size();
        final int maxY = self.getShape().get(0).length;
        final int maxX = self.getShape().get(0)[0].length();

        BlockPos end = setActualRelativeOffset(maxX, maxY, maxZ, EnumFacing.SOUTH, EnumFacing.UP,
                true, structureDir);
        BlockPos addition = new BlockPos(end.getX() < 0 ? -end.getX() - 1 : 0, end.getY() < 0 ? -end.getY() - 1 : 0,
                end.getZ() < 0 ? -end.getZ() - 1 : 0);
        BlockPos bound = new BlockPos(Math.abs(end.getX()), Math.abs(end.getY()), Math.abs(end.getZ()));
        BlockInfo[][][] blockInfos = new BlockInfo[bound.getX()][bound.getY()][bound.getZ()];
        for (int z = 0; z < maxZ; z++) {
            String[] aisleEntry = self.getShape().get(z);
            for (int y = 0; y < maxY; y++) {
                String columnEntry = aisleEntry[y];
                for (int x = 0; x < maxX; x++) {
                    BlockInfo info = self.getSymbolMap().getOrDefault(columnEntry.charAt(x), BlockInfo.EMPTY);
                    TileEntity tileEntity = info.getTileEntity();
                    if (tileEntity instanceof MetaTileEntityHolder holder) {
                        final MetaTileEntity mte = holder.getMetaTileEntity();
                        holder = new MetaTileEntityHolder();
                        holder.setMetaTileEntity(mte);
                        holder.getMetaTileEntity().onPlacement();
                        holder.getMetaTileEntity().setFrontFacing(mte.getFrontFacing());
                        info = new BlockInfo(info.getBlockState(), holder);
                    } else if (tileEntity != null) {
                        info = new BlockInfo(info.getBlockState(), tileEntity);
                    }
                    BlockPos pos = setActualRelativeOffset(x, y, z, EnumFacing.SOUTH,
                            EnumFacing.UP, true, structureDir).add(addition);
                    blockInfos[pos.getX()][pos.getY()][pos.getZ()] = info;
                }
            }
        }
        return blockInfos;
    }

    @Override
    public DirectionalShapeInfoBuilder shallowCopy() {
        DirectionalShapeInfoBuilder builder = new DirectionalShapeInfoBuilder(this.structureDir);
        var builderAccessor = (MultiblockShapeInfoBuilderAccessor) builder;
        builderAccessor.setShape(new ArrayList<>(self.getShape()));
        builderAccessor.setSymbolMap(new HashMap<>(self.getSymbolMap()));
        return builder;
    }

    @Override
    public MultiblockShapeInfo build() {
        return new MultiblockShapeInfo(bakeArray());
    }

    public static BlockPos setActualRelativeOffset(int x, int y, int z, EnumFacing facing, EnumFacing upwardsFacing,
                                                   boolean isFlipped, RelativeDirection[] structureDir) {
        int[] c0 = new int[] { x, y, z }, c1 = new int[3];
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
            EnumFacing of = facing == EnumFacing.DOWN ? upwardsFacing : upwardsFacing.getOpposite();
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualFacing(of)) {
                    case UP -> c1[1] = c0[i];
                    case DOWN -> c1[1] = -c0[i];
                    case WEST -> c1[0] = -c0[i];
                    case EAST -> c1[0] = c0[i];
                    case NORTH -> c1[2] = -c0[i];
                    case SOUTH -> c1[2] = c0[i];
                }
            }
            int xOffset = upwardsFacing.getXOffset();
            int zOffset = upwardsFacing.getZOffset();
            int tmp;
            if (xOffset == 0) {
                tmp = c1[2];
                c1[2] = zOffset > 0 ? c1[1] : -c1[1];
                c1[1] = zOffset > 0 ? -tmp : tmp;
            } else {
                tmp = c1[0];
                c1[0] = xOffset > 0 ? c1[1] : -c1[1];
                c1[1] = xOffset > 0 ? -tmp : tmp;
            }
            if (isFlipped) {
                if (upwardsFacing == EnumFacing.NORTH || upwardsFacing == EnumFacing.SOUTH) {
                    c1[0] = -c1[0]; // flip X-axis
                } else {
                    c1[2] = -c1[2]; // flip Z-axis
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualFacing(facing)) {
                    case UP -> c1[1] = c0[i];
                    case DOWN -> c1[1] = -c0[i];
                    case WEST -> c1[0] = -c0[i];
                    case EAST -> c1[0] = c0[i];
                    case NORTH -> c1[2] = -c0[i];
                    case SOUTH -> c1[2] = c0[i];
                }
            }
            if (upwardsFacing == EnumFacing.WEST || upwardsFacing == EnumFacing.EAST) {
                int xOffset = upwardsFacing == EnumFacing.WEST ? facing.rotateY().getXOffset() :
                        facing.rotateY().getOpposite().getXOffset();
                int zOffset = upwardsFacing == EnumFacing.WEST ? facing.rotateY().getZOffset() :
                        facing.rotateY().getOpposite().getZOffset();
                int tmp;
                if (xOffset == 0) {
                    tmp = c1[2];
                    c1[2] = zOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = zOffset > 0 ? tmp : -tmp;
                } else {
                    tmp = c1[0];
                    c1[0] = xOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = xOffset > 0 ? tmp : -tmp;
                }
            } else if (upwardsFacing == EnumFacing.SOUTH) {
                c1[1] = -c1[1];
                if (facing.getXOffset() == 0) {
                    c1[0] = -c1[0];
                } else {
                    c1[2] = -c1[2];
                }
            }
            if (isFlipped) {
                if (upwardsFacing == EnumFacing.NORTH || upwardsFacing == EnumFacing.SOUTH) {
                    if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
                        c1[0] = -c1[0]; // flip X-axis
                    } else {
                        c1[2] = -c1[2]; // flip Z-axis
                    }
                } else {
                    c1[1] = -c1[1]; // flip Y-axis
                }
            }
        }
        return new BlockPos(c1[0], c1[1], c1[2]);
    }
}
