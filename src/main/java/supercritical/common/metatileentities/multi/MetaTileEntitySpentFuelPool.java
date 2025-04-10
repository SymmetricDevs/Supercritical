package supercritical.common.metatileentities.multi;

import static gregtech.api.util.RelativeDirection.*;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import supercritical.api.recipes.SCRecipeMaps;
import supercritical.client.renderer.textures.SCTextures;
import supercritical.common.blocks.BlockNuclearCasing;
import supercritical.common.blocks.SCMetaBlocks;

public class MetaTileEntitySpentFuelPool extends RecipeMapMultiblockController {

    public static final int PARALLEL_PER_LENGTH = 32;

    public MetaTileEntitySpentFuelPool(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SCRecipeMaps.SPENT_FUEL_POOL_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySpentFuelPool(metaTileEntityId);
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    private static IBlockState getRodState() {
        return SCMetaBlocks.NUCLEAR_CASING.getState(BlockNuclearCasing.NuclearCasingType.SPENT_FUEL_CASING);
    }

    private static IBlockState getMetalCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.recipeMapWorkable.setParallelLimit(structurePattern.formedRepetitionCount[0] * PARALLEL_PER_LENGTH);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                // spotless:off
                .aisle("CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "TTTTTTTTTT")
                .aisle("CCCCCCCCCC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CUUUUUUUUC", "S........T")
                .aisle("CCCCCCCCCC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CUUUUUUUUC", "T........T")
                .setRepeatable(1, 10)
                .aisle("CCCCCCCCCC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CUUUUUUUUC", "T........T")
                .aisle("CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "TTTTTTTTTT")
                //spotless:on
                .where('S', selfPredicate())
                .where('.', any())
                .where('C', blocks(SCMetaBlocks.PANELLING))
                .where('W', blocks(Blocks.WATER).or(blocks(Blocks.FLOWING_WATER)))
                .where('U', blocks(Blocks.WATER))
                .where('R', states(getRodState()))
                .where('T',
                        states(getMetalCasingState()).or(autoAbilities(true, false, true, true, false, true, false)))
                .build();
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SCTextures.SPENT_FUEL_POOL_OVERLAY;
    }

    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("supercritical.machine.spent_fuel_pool.tooltip.parallel", PARALLEL_PER_LENGTH));
    }
}
