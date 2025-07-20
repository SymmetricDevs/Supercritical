package supercritical.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.metatileentity.multiblock.SCMultiblockAbility;
import supercritical.api.nuclear.fission.IModeratorStats;
import supercritical.api.nuclear.fission.ModeratorRegistry;
import supercritical.client.renderer.textures.SCTextures;
import supercritical.common.blocks.BlockFissionCasing;
import supercritical.common.blocks.SCMetaBlocks;

import java.util.List;

public class MetaTileEntityModeratorPort extends MetaTileEntityMultiblockNotifiablePart
        implements IFissionReactorHatch,
        IMultiblockAbilityPart<MetaTileEntityModeratorPort> {

    @Getter
    private IModeratorStats moderator;

    public MetaTileEntityModeratorPort(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 4, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityModeratorPort(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public boolean checkValidity(int depth) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        IBlockState defaultState = getWorld().getBlockState(pos.move(this.frontFacing.getOpposite()));
        IModeratorStats stats = ModeratorRegistry.getModerator(defaultState);
        this.moderator = stats;
        if (stats == null) {
            return false;
        }
        for (int i = 2; i < depth; i++) {
            if (getWorld().getBlockState(pos.move(this.frontFacing.getOpposite())).equals(defaultState)) {
                return false;
            }
        }
        return getWorld().getBlockState(pos.move(this.frontFacing.getOpposite())) ==
                SCMetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL);
    }

    @Override
    public MultiblockAbility<MetaTileEntityModeratorPort> getAbility() {
        return SCMultiblockAbility.MODERATOR_PORT;
    }

    @Override
    public void registerAbilities(List<MetaTileEntityModeratorPort> abilityList) {
        abilityList.add(this);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format(this.getMetaName() + ".tooltip.1"));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SCTextures.MODERATOR_PORT.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }
}
