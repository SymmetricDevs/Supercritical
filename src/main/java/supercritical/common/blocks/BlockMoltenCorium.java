package supercritical.common.blocks;

import static supercritical.api.unification.material.SCMaterials.Corium;

import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggableFluid;
import gregtech.api.fluids.GTFluidBlock;
import gregtech.api.fluids.GTFluidMaterial;
import gregtech.api.fluids.GTFluidRegistration;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import supercritical.SCInternalTags;

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
    public Boolean isEntityInsideMaterial(
                                          @NotNull IBlockAccess world,
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
}
