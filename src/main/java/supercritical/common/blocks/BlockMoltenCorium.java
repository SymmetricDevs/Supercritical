package supercritical.common.blocks;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggableFluid;
import gregtech.api.fluids.GTFluidBlock;
import gregtech.api.fluids.GTFluidMaterial;
import gregtech.api.fluids.GTFluidRegistration;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supercritical.SCInternalTags;

import java.util.Random;

import static supercritical.api.unification.material.SCMaterials.Corium;

@Optional.Interface(modid = "fluidlogged_api",
        iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggableFluid")
public class BlockMoltenCorium extends GTFluidBlock implements IFluidloggableFluid {

    public BlockMoltenCorium(@NotNull Fluid fluid, @NotNull MaterialLiquid material, @NotNull Material gtMaterial) {
        super(fluid, material, gtMaterial);
    }

    @NotNull
    public static BlockMoltenCorium register() {
        Fluid fluid = Corium.getFluid();
        MaterialLiquid liquidMaterial = new GTFluidMaterial(GTUtility.getMapColor(fluid.getColor()), false);
        BlockMoltenCorium block = new BlockMoltenCorium(fluid, liquidMaterial, Corium);

        block.setDensity(fluid.getDensity());
        block.setTemperature(fluid.getTemperature());
        block.setMaxScaledLight(fluid.getLuminosity());
        block.setTickRate(fluid.getViscosity() / 200);

        block.setRenderLayer(BlockRenderLayer.SOLID);
        block.setLightOpacity(255);
        block.setRegistryName(SCInternalTags.MODID, "fluid." + fluid.getName());

        GTFluidRegistration.INSTANCE.registerFluidBlock(block);
        fluid.setBlock(block);
        return block;
    }

    @Nullable
    @Override
    public Boolean isEntityInsideMaterial(@NotNull IBlockAccess world,
                                          @NotNull BlockPos blockpos,
                                          @NotNull IBlockState iblockstate,
                                          @NotNull Entity entity,
                                          double yToTest,
                                          @NotNull net.minecraft.block.material.Material materialIn,
                                          boolean testingHead) {
        return materialIn == net.minecraft.block.material.Material.LAVA ? true : null;
    }

    @Override
    public boolean isBurning(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        return true;
    }


    @Override
    @Optional.Method(modid = "fluidlogged_api")
    public boolean isFluidloggableFluid() {
        return false;
    }

    // Copied from BlockStaticLiquid.
    @Override
    public void updateTick(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Random rand) {
        if (worldIn.getGameRules().getBoolean("doFireTick")) {
            int i = rand.nextInt(3);

            if (i > 0) {
                BlockPos blockpos = pos;

                for (int j = 0; j < i; ++j) {
                    blockpos = blockpos.add(rand.nextInt(3) - 1, 1, rand.nextInt(3) - 1);

                    if (blockpos.getY() >= 0 && blockpos.getY() < worldIn.getHeight() && !worldIn.isBlockLoaded(blockpos)) {
                        return;
                    }

                    IBlockState block = worldIn.getBlockState(blockpos);

                    if (block.getBlock().isAir(block, worldIn, blockpos)) {
                        if (this.isSurroundingBlockFlammable(worldIn, blockpos)) {
                            worldIn.setBlockState(blockpos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, blockpos, pos, Blocks.FIRE.getDefaultState()));
                            return;
                        }
                    } else if (block.getMaterial().blocksMovement()) {
                        return;
                    }
                }
            } else {
                for (int k = 0; k < 3; ++k) {
                    BlockPos blockpos1 = pos.add(rand.nextInt(3) - 1, 0, rand.nextInt(3) - 1);

                    if (blockpos1.getY() >= 0 && blockpos1.getY() < 256 && !worldIn.isBlockLoaded(blockpos1)) {
                        return;
                    }

                    if (worldIn.isAirBlock(blockpos1.up()) && this.getCanBlockBurn(worldIn, blockpos1)) {
                        worldIn.setBlockState(blockpos1.up(), net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(worldIn, blockpos1.up(), pos, Blocks.FIRE.getDefaultState()));
                    }
                }
            }

        }

    }

    protected boolean isSurroundingBlockFlammable(World worldIn, BlockPos pos) {
        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (this.getCanBlockBurn(worldIn, pos.offset(enumfacing))) {
                return true;
            }
        }
        return false;
    }

    private boolean getCanBlockBurn(World worldIn, BlockPos pos) {
        return pos.getY() >= 0 && pos.getY() < 256 && !worldIn.isBlockLoaded(pos) ? false : worldIn.getBlockState(pos).getMaterial().getCanBurn();
    }
}
