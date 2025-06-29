package supercritical.common.metatileentities.multi.multiblockpart;

import static supercritical.SCValues.FISSION_LOCK_UPDATE;

import java.io.IOException;
import java.util.List;

import gregtech.api.gui.widgets.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import supercritical.api.capability.IFuelRodHandler;
import supercritical.api.items.itemhandlers.LockableItemStackHandler;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.metatileentity.multiblock.SCMultiblockAbility;
import supercritical.api.nuclear.fission.FissionFuelRegistry;
import supercritical.api.nuclear.fission.IFissionFuelStats;
import supercritical.api.nuclear.fission.components.FuelRod;
import supercritical.common.blocks.BlockFissionCasing;
import supercritical.common.blocks.SCMetaBlocks;
import supercritical.common.metatileentities.multi.MetaTileEntityFissionReactor;

public class MetaTileEntityFuelRodImportBus extends MetaTileEntityMultiblockNotifiablePart
                                            implements IMultiblockAbilityPart<IFuelRodHandler>, IFuelRodHandler,
                                            IControllable, IFissionReactorHatch {

    private boolean workingEnabled;
    private IFissionFuelStats fuelProperty;
    public MetaTileEntityFuelRodExportBus pairedHatch;
    private IFissionFuelStats partialFuel;
    private FuelRod internalFuelRod;
    private double depletionPoint;
    private IItemHandlerModifiable partialFuelDisplay;

    public MetaTileEntityFuelRodImportBus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 4, false);
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.workingEnabled = isWorkingAllowed;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFuelRodImportBus(metaTileEntityId);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new LockableItemStackHandler(this, false);
    }

    private ModularUI.Builder createUITemplate(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 143).label(10, 5, getMetaFullName());

        builder.widget(new BlockableSlotWidget(importItems, 0, 40, 18, true, true)
                .setIsBlocked(this::isLocked).setBackgroundTexture(GuiTextures.SLOT));

        builder.widget(new SlotWidget(partialFuelDisplay, 0, 118, 18, false, false)
                .setBackgroundTexture(GuiTextures.MAINTENANCE_ICON));

        builder.widget(new ClickButtonWidget(140, 18, 18, 18, "", (d) -> voidPartialFuel())
                .setTooltipText("fuelbus.void")
                .setButtonTexture(GuiTextures.BUTTON_VOID_NONE)
                .setShouldClientCallback(true));

        builder.widget(new AdvancedTextWidget(10, 43, (list) -> {
            list.add(new TextComponentTranslation("supercritical.gui.fission.depletion", String.format("%.2f", getCurrentDepletionRatio() * 100)));
        }, 0));

        return builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 60);
    }

    public double getCurrentDepletionRatio() {
        if (this.partialFuel == null)
            return 0;
        if (this.getController() == null || !(this.getController() instanceof MetaTileEntityFissionReactor reactor) || !reactor.isLocked())
            return 1 - (depletionPoint / this.partialFuel.getDuration());
        return 1 - ((depletionPoint - reactor.getTotalDepletion()) / this.partialFuel.getDuration());
    }

    public void voidPartialFuel() {
        if (this.getController() != null &&
                ((MetaTileEntityFissionReactor) this.getController()).isLocked())
            return;
        setPartialFuel(null);
        depletionPoint = 0;
        setLock(false);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        partialFuelDisplay = new ItemStackHandler(1) {
            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                return getLockedObject();
            }
        };
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
            pullItemsFromNearbyHandlers(getFrontFacing());
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public MultiblockAbility<IFuelRodHandler> getAbility() {
        return SCMultiblockAbility.IMPORT_FUEL_ROD;
    }

    @Override
    public void registerAbilities(List<IFuelRodHandler> abilityList) {
        abilityList.add(this);
    }

    @Override
    public boolean checkValidity(int depth) {
        this.pairedHatch = getExportHatch(depth);
        return pairedHatch != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        getLockedImport().setLock(data.getBoolean("locked"));
        if (data.hasKey("partialFuel")) {
            this.partialFuel = FissionFuelRegistry.getFissionFuel(data.getString("partialFuel"));
        }
        depletionPoint = data.getDouble("depletionPoint");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("locked", getLockedImport().isLocked());
        if (partialFuel != null) data.setString("partialFuel", this.partialFuel.getID());
        data.setDouble("depletionPoint", depletionPoint);
        return super.writeToNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeItemStack(getLockedImport().getStackInSlot(0));
        buf.writeBoolean(getLockedImport().isLocked());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            getLockedImport().setStackInSlot(0, buf.readItemStack());
        } catch (IOException e) { // ignored
        }
        getLockedImport().setLock(buf.readBoolean());
    }

    private LockableItemStackHandler getLockedImport() {
        return (LockableItemStackHandler) importItems;
    }

    @Override
    public void setLock(boolean isLocked) {
        if (depletionPoint == 0) {
            getLockedImport().setLock(isLocked);
            writeCustomData(FISSION_LOCK_UPDATE, (packetBuffer -> {
                packetBuffer.writeBoolean(isLocked);
            }));
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == FISSION_LOCK_UPDATE) {
            getLockedImport().setLock(buf.readBoolean());
        }
    }

    @Override
    public boolean isLocked() {
        return getLockedImport().isLocked();
    }

    @Override
    public ItemStack getLockedObject() {
        return getLockedImport().getLockedObject();
    }

    @Override
    public IFissionFuelStats getFuel() {
        return this.fuelProperty;
    }

    @Override
    public void setFuel(IFissionFuelStats prop) {
        this.fuelProperty = prop;
    }

    @Override
    public IFissionFuelStats getPartialFuel() {
        return this.partialFuel;
    }

    @Override
    public boolean setPartialFuel(IFissionFuelStats prop) {
        if (prop == this.partialFuel) {
            return false;
        }
        this.partialFuel = prop;
        if (prop == null) {
            this.internalFuelRod = null;
        } else if (this.internalFuelRod != null) {
            this.internalFuelRod.setFuel(prop);
        }
        return true;
    }

    @Override
    public void setInternalFuelRod(FuelRod rod) {
        this.internalFuelRod = rod;
    }

    @Override
    public boolean isDepleted(double totalDepletion) {
        return this.depletionPoint <= totalDepletion;
    }

    @Override
    public void markUndepleted() {
        this.depletionPoint += this.partialFuel.getDuration();
    }

    @Override
    public LockableItemStackHandler getInputStackHandler() {
        return this.getLockedImport();
    }

    public IItemHandlerModifiable getOutputStackHandler(int depth) {
        return this.getExportHatch(depth).getExportItems();
    }

    @Override
    public void resetDepletion(double fuelDepletion) {
        this.depletionPoint -= fuelDepletion;
    }

    @Override
    public double getDepletionPoint() {
        return this.depletionPoint;
    }

    public MetaTileEntityFuelRodExportBus getExportHatch(int depth) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        for (int i = 1; i < depth; i++) {
            if (getWorld().getBlockState(pos.move(this.frontFacing.getOpposite())) !=
                    SCMetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.FUEL_CHANNEL)) {
                return null;
            }
        }
        if (getWorld().getTileEntity(pos.move(this.frontFacing.getOpposite())) instanceof IGregTechTileEntity gtTe) {
            MetaTileEntity mte = gtTe.getMetaTileEntity();
            if (mte instanceof MetaTileEntityFuelRodExportBus) {
                return (MetaTileEntityFuelRodExportBus) mte;
            }
        }
        return null;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("supercritical.machine.nuclear.locking.item"));
    }
}
