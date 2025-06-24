package supercritical.common.metatileentities.multi;

import static gregtech.api.util.RelativeDirection.*;
import static supercritical.api.pattern.SCPredicates.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.TextComponentUtil;
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

    private boolean waterFilled;
    private List<BlockPos> waterPositions;

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
        // noinspection DataFlowIssue
        this.recipeMapWorkable.setParallelLimit(structurePattern.formedRepetitionCount[2] * PARALLEL_PER_LENGTH);

        this.waterPositions = context.getOrDefault(FLUID_BLOCKS_KEY, new ArrayList<>());
        this.waterPositions.sort(Comparator.comparingInt(BlockPos::getY));
        this.waterFilled = waterPositions.isEmpty();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.waterPositions = null; // Clear water fill data when the structure is invalidated
        this.waterFilled = false;
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        if (!waterFilled && getOffsetTimer() % 5 == 0) {
            fillFluid(this, this.waterPositions, FluidRegistry.WATER);
            if (this.waterPositions.isEmpty()) {
                this.waterFilled = true;
            }
        }
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || !waterFilled;
    }

    public boolean isWaterFilled() {
        return waterFilled;
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
                .aisle("CCCCCCCCCC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "S........T")
                .aisle("CCCCCCCCCC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "T........T")
                .setRepeatable(1, 10)
                .aisle("CCCCCCCCCC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "T........T")
                .aisle("CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "TTTTTTTTTT")
                //spotless:on
                .where('S', selfPredicate())
                .where('.', any())
                .where('C', blocks(SCMetaBlocks.PANELLING))
                .where('W', fluid(FluidRegistry.WATER))
                .where('R', states(getRodState()))
                .where('T',
                        states(getMetalCasingState()).or(autoAbilities())
                                .or(abilities(MultiblockAbility.IMPORT_FLUIDS)))
                .build();
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SCTextures.SPENT_FUEL_POOL_OVERLAY;
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (isStructureFormed() && !waterFilled) {
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.RED,
                    "supercritical.multiblock.spent_fuel_pool.obstructed"));
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "supercritical.multiblock.spent_fuel_pool.obstructed.desc"));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("supercritical.machine.spent_fuel_pool.tooltip.parallel", PARALLEL_PER_LENGTH));
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@NotNull IMultiblockPart part) {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
