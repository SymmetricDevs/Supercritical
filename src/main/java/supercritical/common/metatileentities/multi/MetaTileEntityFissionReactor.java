package supercritical.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.*;
import gregtech.api.util.*;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenCustomHashMap;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supercritical.SCValues;
import supercritical.api.capability.ICoolantHandler;
import supercritical.api.capability.IFuelRodHandler;
import supercritical.api.cover.ICustomEnergyCover;
import supercritical.api.gui.SCGuiTextures;
import supercritical.api.gui.widgets.UpdatedSliderWidget;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.metatileentity.multiblock.SCMultiblockAbility;
import supercritical.api.nuclear.fission.*;
import supercritical.api.nuclear.fission.components.ControlRod;
import supercritical.api.nuclear.fission.components.CoolantChannel;
import supercritical.api.nuclear.fission.components.FuelRod;
import supercritical.api.nuclear.fission.components.Moderator;
import supercritical.api.pattern.DirectionalShapeInfoBuilder;
import supercritical.api.unification.material.SCMaterials;
import supercritical.api.util.SCUtility;
import supercritical.client.renderer.textures.SCTextures;
import supercritical.common.SCConfigHolder;
import supercritical.common.blocks.BlockFissionCasing;
import supercritical.common.blocks.SCMetaBlocks;
import supercritical.common.metatileentities.SCMetaTileEntities;
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityControlRodPort;
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityModeratorPort;

import java.util.*;

public class MetaTileEntityFissionReactor extends MultiblockWithDisplayBase
        implements IDataInfoProvider, IProgressBarMultiblock, ICustomEnergyCover {

    private FissionReactor fissionReactor;
    private int diameter;
    private int heightTop;
    private int heightBottom;
    private int height;
    // Used for maintenance mechanics
    private boolean isFlowingCorrectly = true;
    private LockingState lockingState = LockingState.UNLOCKED;

    private double kEff;

    @Getter
    private double totalDepletion;
    @Getter
    private double controlRodInsertion;
    @Getter
    private double temperature;
    @Getter
    private double maxTemperature;
    @Getter
    private double pressure;
    @Getter
    private double maxPressure;
    @Getter
    private double power;
    @Getter
    private double maxPower;

    private NBTTagCompound transientData;

    public MetaTileEntityFissionReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFissionReactor(metaTileEntityId);
    }

    @NotNull
    protected static IBlockState getVesselState() {
        return SCMetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL);
    }

    @Override
    public double getFillPercentage(int index) {
        if (index == 0) {
            return this.temperature / this.maxTemperature;
        } else if (index == 1) {
            return this.pressure / this.maxPressure;
        } else {
            if (this.maxPower / this.power > Math.exp(9)) {
                return 0;
            }
            return (Math.log(this.power / this.maxPower) + 9) / 9;
        }
    }

    @NotNull
    protected static IBlockState getFuelChannelState() {
        return SCMetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.FUEL_CHANNEL);
    }

    protected static TraceabilityPredicate moderatorPredicate() {
        return new TraceabilityPredicate(
                (state) -> ModeratorRegistry.getModerator(state.getBlockState()) != null
        );
    }

    /**
     * Public for OC integration, use it if you want ig
     */
    public void toggleControlRodRegulation(boolean b) {
        if (fissionReactor != null) {
            this.fissionReactor.controlRodRegulationOn = b;
        }
    }

    public boolean areControlRodsRegulated() {
        return fissionReactor != null && this.fissionReactor.controlRodRegulationOn;
    }

    @NotNull
    protected static IBlockState getControlRodChannelState() {
        return SCMetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.CONTROL_ROD_CHANNEL);
    }

    public void setControlRodInsertion(float value) {
        this.controlRodInsertion = value;
        if (fissionReactor != null)
            fissionReactor.updateControlRodInsertion(controlRodInsertion);
    }

    public boolean isLocked() {
        return lockingState == LockingState.LOCKED;
    }

    @NotNull
    protected static IBlockState getCoolantChannelState() {
        return SCMetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.COOLANT_CHANNEL);
    }

    private void tryLocking(boolean lock) {
        if (!isStructureFormed())
            return;

        if (lock)
            lockAndPrepareReactor();
        else
            unlockAll();
    }

    @Override
    public void addBarHoverText(List<ITextComponent> list, int index) {
        if (index == 0) {
            list.add(new TextComponentTranslation("supercritical.gui.fission.temperature",
                    String.format("%.1f", this.temperature) + " / " + String.format("%.1f", this.maxTemperature)));
        } else if (index == 1) {
            list.add(new TextComponentTranslation("supercritical.gui.fission.pressure",
                    String.format("%.0f", this.pressure) + " / " + String.format("%.0f", this.maxPressure)));
        } else {
            list.add(new TextComponentTranslation("supercritical.gui.fission.power", String.format("%.1f", this.power),
                    String.format("%.1f", this.maxPower)));
        }
    }

    @Override
    protected void addErrorText(List<ITextComponent> list) {
        if (lockingState != LockingState.LOCKED && lockingState != LockingState.UNLOCKED) {
            list.add(
                    new TextComponentTranslation(
                            "supercritical.gui.fission.lock." + lockingState.toString().toLowerCase()));
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> list) {
        super.addDisplayText(list);
        list.add(
                TextComponentUtil.setColor(new TextComponentTranslation(
                                "supercritical.gui.fission.lock." + lockingState.toString().toLowerCase()),
                        getLockedTextColor()));
        list.add(new TextComponentTranslation("supercritical.gui.fission.k_eff", String.format("%.4f", this.kEff)));
    }

    protected EnumFacing getUp() {
        return RelativeDirection.UP.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
    }

    protected EnumFacing getRight() {
        return RelativeDirection.RIGHT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
    }

    /**
     * Uses the upper layer to determine the diameter of the structure
     */
    protected int findDiameter(int heightTop) {
        int i = 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        pos.move(getUp(), heightTop);
        while (i <= 15) {
            if (this.isBlockEdge(this.getWorld(), pos,
                    this.getFrontFacing().getOpposite(),
                    i))
                break;
            i++;
        }
        return i;
    }

    /**
     * Checks for casings on top or bottom of the controller to determine the height of the reactor
     */
    protected int findHeight(boolean top) {
        int i = 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        while (i <= 15) {
            if (this.isBlockEdge(this.getWorld(), pos, top ? getUp() : getUp().getOpposite(), i))
                break;
            i++;
        }
        return i - 1;
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 240, 208);

        // Display
        builder.image(4, 4, 232, 109, GuiTextures.DISPLAY);

        // triple bar
        ProgressWidget progressBar = new ProgressWidget(
                () -> this.getFillPercentage(0),
                4, 115, 76, 7,
                SCGuiTextures.PROGRESS_BAR_FISSION_HEAT, ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> this.addBarHoverText(list, 0));
        builder.widget(progressBar);

        progressBar = new ProgressWidget(
                () -> this.getFillPercentage(1),
                82, 115, 76, 7,
                SCGuiTextures.PROGRESS_BAR_FISSION_PRESSURE, ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> this.addBarHoverText(list, 1));
        builder.widget(progressBar);

        progressBar = new ProgressWidget(
                () -> this.getFillPercentage(2),
                160, 115, 76, 7,
                SCGuiTextures.PROGRESS_BAR_FISSION_ENERGY, ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> this.addBarHoverText(list, 2));
        builder.widget(progressBar);

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);

        builder.widget(new UpdatedSliderWidget("supercritical.gui.fission.control_rod_insertion", 10, 60, 220,
                18, 0.0f, 1.0f,
                (float) controlRodInsertion, this::setControlRodInsertion,
                () -> (float) this.controlRodInsertion) {

            @Override
            protected String getDisplayString() {
                return I18n.format("supercritical.gui.fission.control_rod_insertion",
                        String.format("%.2f%%", this.getSliderValue() * 100));
            }
        }.setBackground(SCGuiTextures.DARK_SLIDER_BACKGROUND).setSliderIcon(SCGuiTextures.DARK_SLIDER_ICON));

        builder.widget(new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(220)
                .setClickHandler(this::handleDisplayClick));

        // Power Button

        builder.widget(new ToggleButtonWidget(215, 183, 18, 18, GuiTextures.BUTTON_LOCK,
                this::isLocked, this::tryLocking).shouldUseBaseBackground()
                .setTooltipText("supercritical.gui.fission.lock"));
        builder.widget(new ImageWidget(215, 201, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));

        // Voiding Mode Button
        builder.widget(new ImageWidget(215, 161, 18, 18, GuiTextures.BUTTON_VOID_NONE)
                .setTooltip("gregtech.gui.multiblock_voiding_not_supported"));

        builder.widget(new ImageWidget(215, 143, 18, 18, GuiTextures.BUTTON_NO_DISTINCT_BUSES)
                .setTooltip("gregtech.multiblock.universal.distinct_not_supported"));

        // Flex Button
        builder.widget(getFlexButton(215, 125, 18, 18));

        builder.bindPlayerInventory(entityPlayer.inventory, 125);
        return builder;
    }

    @Override
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        return new ToggleButtonWidget(x, y, width, height, this::areControlRodsRegulated,
                this::toggleControlRodRegulation).setButtonTexture(SCGuiTextures.BUTTON_CONTROL_ROD_HELPER)
                .setTooltipText("supercritical.gui.fission.helper");
    }

    private TextFormatting getLockedTextColor() {
        return switch (lockingState) {
            case LOCKED -> TextFormatting.GREEN;
            case UNLOCKED -> TextFormatting.DARK_AQUA;
            case INVALID_COMPONENT -> TextFormatting.RED;
            case SHOULD_LOCK -> TextFormatting.BLACK;
            default -> getWorld().getWorldTime() % 4 >= 2 ? TextFormatting.RED : TextFormatting.YELLOW;
        };
    }

    protected void performPrimaryExplosion() {
        this.unlockAll();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        pos = pos.move(this.getFrontFacing().getOpposite(), diameter / 2);
        this.getWorld().createExplosion(null, pos.getX(), pos.getY() + heightTop, pos.getZ(), 4.f, true);
    }

    protected void performSecondaryExplosion(double accumulatedHydrogen) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getPos());
        pos = pos.move(this.getFrontFacing().getOpposite(), diameter / 2);
        this.getWorld().newExplosion(null, pos.getX(), pos.getY() + heightTop + 3, pos.getZ(),
                5.f + (float) Math.log(accumulatedHydrogen), true, true);
    }

    protected boolean isBlockEdge(@NotNull World world, @NotNull BlockPos.MutableBlockPos pos,
                                  @NotNull EnumFacing direction,
                                  int steps) {
        pos.move(direction, steps);

        if (world.getBlockState(pos).getBlock() == SCMetaBlocks.FISSION_CASING) {
            pos.move(direction.getOpposite(), steps);
            return false;
        }

        MetaTileEntity potentialTile = GTUtility.getMetaTileEntity(world, pos);
        pos.move(direction.getOpposite(), steps);
        if (potentialTile == null) {
            return true;
        }

        return !(potentialTile instanceof IFissionReactorHatch || potentialTile instanceof IMaintenanceHatch);
    }

    @Override
    public void updateFormedValid() {
        // Take in coolant, take in fuel, update reactor, output steam

        if (!this.getWorld().isRemote && this.getOffsetTimer() % 20 == 0) {
            if (this.lockingState == LockingState.LOCKED) {
                // Coolant handling
                if (this.getOffsetTimer() % 100 == 0) {
                    if (isFlowingCorrectly) {
                        if (getWorld().rand.nextDouble() > (1 - 0.01 * this.getNumMaintenanceProblems())) {
                            isFlowingCorrectly = false;
                        }
                    } else {
                        if (getWorld().rand.nextDouble() > 0.12 * this.getNumMaintenanceProblems()) {
                            isFlowingCorrectly = true;
                        }
                    }
                }

                // Fuel handling
                boolean canWork = true;
                for (IFuelRodHandler fuelImport : this.getAbilities(SCMultiblockAbility.IMPORT_FUEL_ROD)) {
                    if (fuelImport.isDepleted(this.fissionReactor.fuelDepletion)) {
                        // There are a few things that could cause the reactor to stop working when a fuel rod becomes
                        // depleted:
                        // The output is blocked
                        // The input is missing
                        // We simulate both of these things, and if it fails, we unlock the entire reactor.
                        if (!fuelImport.getOutputStackHandler(this.height - 1)
                                .insertItem(0, FissionFuelRegistry.getDepletedFuel(fuelImport.getFuel()), true)
                                .isEmpty()) {
                            canWork = false;
                            this.setLockingState(LockingState.FUEL_CLOGGED);
                            break;
                        }
                        fuelImport.getOutputStackHandler(this.height - 1).insertItem(0,
                                FissionFuelRegistry.getDepletedFuel(fuelImport.getFuel()), false);
                        fuelImport.markUndepleted();
                        if (fuelImport.getInputStackHandler().extractItem(0, 1, true).isEmpty()) {
                            canWork = false;
                            fuelImport.setPartialFuel(null); // Clear the partial fuel; it wouldn't have existed
                            this.setLockingState(LockingState.MISSING_FUEL);
                            break;
                        }
                        fuelImport.getInputStackHandler().extractItem(0, 1, false);
                    }
                }

                if (!canWork) {
                    this.unlockAll();
                }
            }
            this.updateReactorState();

            this.syncReactorStats();

            if (!SCConfigHolder.nuclear.enableMeltdown) {
                return;
            }
            boolean melts = this.fissionReactor.checkForMeltdown();
            boolean explodes = this.fissionReactor.checkForExplosion();
            double hydrogen = this.fissionReactor.accumulatedHydrogen;
            if (melts) {
                this.performMeltdownEffects();
            }
            if (explodes) {
                this.performPrimaryExplosion();
                if (hydrogen > 1) {
                    this.performSecondaryExplosion(hydrogen);
                }
            }
        }
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        list.add(new TextComponentTranslation("supercritical.multiblock.fission_reactor.diameter",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.diameter) + "m")
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        list.add(new TextComponentTranslation("supercritical.multiblock.fission_reactor.height",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(this.height) + "m")
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        return list;
    }

    protected void performMeltdownEffects() {
        this.unlockAll();
        Map<Long, BlockInfo> cache = this.structurePattern.cache;
        Map<BlockPos, Boolean> meltsDown = new Object2BooleanOpenCustomHashMap<>(
                new Hash.Strategy<>() {

                    @Override
                    public int hashCode(BlockPos o) {
                        return o.getX() << 16 + o.getZ();
                    }

                    @Override
                    public boolean equals(BlockPos a, BlockPos b) {
                        if (a == null || b == null) {
                            return false;
                        }
                        return a.getX() == b.getX() && a.getZ() == b.getZ();
                    }
                });
        cache.keySet().forEach(blockPosCached -> {
            BlockPos pos = BlockPos.fromLong(blockPosCached);
            BlockInfo info = cache.get(blockPosCached);
            if (meltsDown.containsKey(pos) && meltsDown.get(pos)) { // Already melted; not worrying about if it was
                // above or not
                return;
            }
            int chance = 10;
            if (pos.getY() == this.getPos().getY() - this.heightBottom) {
                chance = 1;
            } else if (info.getTileEntity() instanceof IGregTechTileEntity mteHolder) {
                if (mteHolder.getMetaTileEntity() instanceof IFuelRodHandler) {
                    chance = 1;
                }
            }
            if (getWorld().rand.nextInt(chance) == 0) {
                meltsDown.put(pos, true);
            }
        });
        for (BlockPos immutPos : meltsDown.keySet()) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(immutPos);
            while (pos.getY() >= this.getPos().getY() - this.heightBottom) {
                this.getWorld().setBlockState(pos, SCMaterials.Corium.getFluid().getBlock().getDefaultState());
                pos.move(EnumFacing.DOWN);
            }
        }
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        this.heightTop = Math.max(Math.min(this.getWorld() != null ? this.findHeight(true) : 1, 7), 1);
        this.heightBottom = Math.max(Math.min(this.getWorld() != null ? this.findHeight(false) : 1, 7), 1);

        this.height = heightTop + heightBottom + 1;

        this.diameter = this.getWorld() != null ? Math.max(Math.min(this.findDiameter(heightTop), 15), 5) : 5;

        int radius = this.diameter % 2 == 0 ? (int) Math.floor(this.diameter / 2.f) :
                Math.round((this.diameter - 1) / 2.f);

        StringBuilder interiorBuilder = new StringBuilder();

        String[] interiorSlice = new String[this.diameter];
        String[] controllerSlice;
        String[] topSlice;
        String[] bottomSlice;

        // First loop over the matrix
        for (int i = 0; i < this.diameter; i++) {
            for (int j = 0; j < this.diameter; j++) {

                if (Math.pow(i - Math.floor(this.diameter / 2.), 2) + Math.pow(j - Math.floor(this.diameter / 2.), 2) <
                        Math.pow(radius + 0.5f, 2)) {
                    interiorBuilder.append('A');
                } else {
                    interiorBuilder.append(' ');
                }
            }

            interiorSlice[i] = interiorBuilder.toString();
            interiorBuilder.setLength(0);
        }

        // Second loop is to detect where to put walls, the controller and I/O, two fewer iterations are needed because
        // two strings always represent two walls on opposite sides
        interiorSlice[this.diameter - 1] = interiorSlice[0] = interiorSlice[0].replace('A', 'B');
        for (int i = 1; i < this.diameter - 1; i++) {
            for (int j = 0; j < this.diameter; j++) {
                if (interiorSlice[i].charAt(j) != 'A') {
                    continue;
                }

                // The integer division is fine here, since we want an odd diameter (say, 5) to go to the middle value
                // (2 in this case)
                int outerI = i + (int) Math.signum(i - (diameter / 2));

                if (Math.pow(outerI - Math.floor(this.diameter / 2.), 2) +
                        Math.pow(j - Math.floor(this.diameter / 2.), 2) >
                        Math.pow(radius + 0.5f, 2)) {
                    interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'B');
                }

                int outerJ = j + (int) Math.signum(j - (diameter / 2));
                if (Math.pow(i - Math.floor(this.diameter / 2.), 2) +
                        Math.pow(outerJ - Math.floor(this.diameter / 2.), 2) >
                        Math.pow(radius + 0.5f, 2)) {
                    interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'B');
                }
            }
        }

        controllerSlice = interiorSlice.clone();
        topSlice = interiorSlice.clone();
        bottomSlice = interiorSlice.clone();
        controllerSlice[0] = controllerSlice[0].substring(0, (int) Math.floor(this.diameter / 2.)) + 'S' +
                controllerSlice[0].substring((int) Math.floor(this.diameter / 2.) + 1);
        for (int i = 0; i < this.diameter; i++) {
            topSlice[i] = topSlice[i].replace('A', 'I');
            bottomSlice[i] = bottomSlice[i].replace('A', 'O');
        }

        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
                .aisle(bottomSlice)
                .aisle(interiorSlice).setRepeatable(heightBottom - 1)
                .aisle(controllerSlice)
                .aisle(interiorSlice).setRepeatable(heightTop - 1)
                .aisle(topSlice)
                .where('S', selfPredicate())
                // A for interior components
                .where('A',
                        states(getFuelChannelState(), getControlRodChannelState(), getCoolantChannelState()).or(air()).or(moderatorPredicate()))
                // I for the inputs on the top
                .where('I',
                        states(getVesselState()).or(getImportPredicate()))
                // O for the outputs on the bottom
                .where('O',
                        states(getVesselState())
                                .or(abilities(SCMultiblockAbility.EXPORT_COOLANT,
                                        SCMultiblockAbility.EXPORT_FUEL_ROD)))
                // B for the vessel blocks on the walls
                .where('B',
                        states(getVesselState())
                                .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setMinGlobalLimited(1)
                                        .setMaxGlobalLimited(1)))
                .where(' ', any())
                .build();
    }

    public TraceabilityPredicate getImportPredicate() {
        MultiblockAbility<?>[] allowedAbilities = {SCMultiblockAbility.IMPORT_COOLANT,
                SCMultiblockAbility.IMPORT_FUEL_ROD,
                SCMultiblockAbility.CONTROL_ROD_PORT,
                SCMultiblockAbility.MODERATOR_PORT};
        return tilePredicate((state, tile) -> {
                    if (!(tile instanceof IMultiblockAbilityPart<?> &&
                            ArrayUtils.contains(allowedAbilities, ((IMultiblockAbilityPart<?>) tile).getAbility()))) {
                        return false;
                    }
                    if (tile instanceof IFissionReactorHatch hatchPart) {
                        if (!hatchPart.checkValidity(height - 1)) {
                            state.setError(new PatternStringError("supercritical.multiblock.pattern.error.hatch_invalid"));
                            return false;
                        }
                        return true;
                    }
                    return false;
                },
                () -> Arrays.stream(allowedAbilities)
                        .flatMap(ability -> MultiblockAbility.REGISTRY.get(ability).stream())
                        .filter(Objects::nonNull).map(tile -> {
                            MetaTileEntityHolder holder = new MetaTileEntityHolder();
                            holder.setMetaTileEntity(tile);
                            holder.getMetaTileEntity().onPlacement();
                            holder.getMetaTileEntity().setFrontFacing(EnumFacing.SOUTH);
                            return new BlockInfo(MetaBlocks.MACHINE.getDefaultState(), holder);
                        }).toArray(BlockInfo[]::new));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return SCTextures.FISSION_REACTOR_TEXTURE;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SCTextures.FISSION_REACTOR_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(),
                true);
    }

    @Override
    public boolean isActive() {
        return isStructureFormed() && lockingState == LockingState.LOCKED;
    }

    @Override
    public void checkStructurePattern() {
        if (!this.isStructureFormed()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    @Override
    public void invalidateStructure() {
        this.unlockAll();
        this.fissionReactor = null;
        this.temperature = 273;
        this.maxTemperature = 273;
        this.power = 0;
        this.kEff = 0;
        this.pressure = 0;
        this.maxPressure = 0;
        this.maxPower = 0;
        super.invalidateStructure();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (fissionReactor == null) {
            fissionReactor = new FissionReactor(this.diameter - 2, this.height - 2, controlRodInsertion);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("diameter", this.diameter);
        data.setInteger("heightTop", this.heightTop);
        data.setInteger("heightBottom", this.heightBottom);
        data.setDouble("controlRodInsertion", this.controlRodInsertion);
        data.setBoolean("locked", this.lockingState == LockingState.LOCKED);
        data.setDouble("kEff", this.kEff);
        if (fissionReactor != null) {
            data.setTag("transientData", this.fissionReactor.serializeNBT());
        }

        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.diameter = data.getInteger("diameter");
        this.heightTop = data.getInteger("heightTop");
        this.heightBottom = data.getInteger("heightBottom");
        this.controlRodInsertion = data.getDouble("controlRodInsertion");
        this.height = this.heightTop + this.heightBottom + 1;
        this.kEff = data.getDouble("kEff");
        if (data.getBoolean("locked")) {
            this.lockingState = LockingState.SHOULD_LOCK;
        }
        if (data.hasKey("transientData")) {
            transientData = data.getCompoundTag("transientData");
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.diameter);
        buf.writeInt(this.heightTop);
        buf.writeInt(this.heightBottom);
        buf.writeDouble(this.controlRodInsertion);
        if (this.lockingState == LockingState.SHOULD_LOCK) {
            if (fissionReactor == null) {
                this.fissionReactor = new FissionReactor(this.diameter - 2, this.height - 2, controlRodInsertion);
            }
            this.lockAndPrepareReactor();
            this.fissionReactor.deserializeNBT(transientData);
        }
        buf.writeBoolean(this.lockingState == LockingState.LOCKED);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.diameter = buf.readInt();
        this.heightTop = buf.readInt();
        this.heightBottom = buf.readInt();
        this.controlRodInsertion = buf.readDouble();
        if (buf.readBoolean()) {
            this.lockingState = LockingState.LOCKED;
        }
    }

    public void syncReactorStats() {
        this.temperature = this.fissionReactor.temperature;
        this.maxTemperature = this.fissionReactor.maxTemperature;
        this.pressure = this.fissionReactor.pressure;
        this.maxPressure = this.fissionReactor.maxPressure;
        this.power = this.fissionReactor.power;
        this.maxPower = this.fissionReactor.maxPower;
        this.kEff = this.fissionReactor.kEff;
        this.controlRodInsertion = this.fissionReactor.controlRodInsertion;
        this.totalDepletion = this.fissionReactor.fuelDepletion;
        writeCustomData(SCValues.SYNC_REACTOR_STATS, (packetBuffer -> {
            packetBuffer.writeDouble(this.temperature);
            packetBuffer.writeDouble(this.maxTemperature);
            packetBuffer.writeDouble(this.pressure);
            packetBuffer.writeDouble(this.maxPressure);
            packetBuffer.writeDouble(this.power);
            packetBuffer.writeDouble(this.maxPower);
            packetBuffer.writeDouble(this.kEff);
            packetBuffer.writeDouble(this.controlRodInsertion);
            packetBuffer.writeDouble(this.totalDepletion);
        }));
        this.markDirty();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);

        if (dataId == SCValues.SYNC_REACTOR_STATS) {
            this.temperature = buf.readDouble();
            this.maxTemperature = buf.readDouble();
            this.pressure = buf.readDouble();
            this.maxPressure = buf.readDouble();
            this.power = buf.readDouble();
            this.maxPower = buf.readDouble();
            this.kEff = buf.readDouble();
            this.controlRodInsertion = buf.readDouble();
            this.totalDepletion = buf.readDouble();
        } else if (dataId == SCValues.SYNC_LOCKING_STATE) {
            this.lockingState = buf.readEnumValue(LockingState.class);
            this.scheduleRenderUpdate();
        }
    }

    protected void lockAll() {
        for (ICoolantHandler handler : this.getAbilities(SCMultiblockAbility.IMPORT_COOLANT)) {
            handler.setLock(true);
        }
        for (IFuelRodHandler handler : this.getAbilities(SCMultiblockAbility.IMPORT_FUEL_ROD)) {
            handler.setLock(true);
        }
    }

    protected void unlockAll() {
        for (ICoolantHandler handler : this.getAbilities(SCMultiblockAbility.IMPORT_COOLANT)) {
            handler.setLock(false);
        }
        for (IFuelRodHandler handler : this.getAbilities(SCMultiblockAbility.IMPORT_FUEL_ROD)) {
            handler.resetDepletion(this.fissionReactor.fuelDepletion); // Must come first
            handler.setLock(false);
        }
        if (this.fissionReactor != null) {
            this.fissionReactor.turnOff();
            this.fissionReactor.resetFuelDepletion();
        }
        if (this.lockingState == LockingState.LOCKED) { // Don't remove warnings
            this.setLockingState(LockingState.UNLOCKED);
        }
    }

    private void lockAndPrepareReactor() {
        if (!verifyCorrectness()) {
            return;
        }
        this.lockAll();
        this.addReactorComponents();
        fissionReactor.prepareThermalProperties();
        fissionReactor.computeGeometry();
        setLockingState(LockingState.LOCKED);
    }

    private boolean verifyCorrectness() {
        boolean foundFuel = false;
        int radius = this.diameter / 2;
        BlockPos.MutableBlockPos reactorOrigin = new BlockPos.MutableBlockPos(this.getPos());
        reactorOrigin.move(this.frontFacing.getOpposite(), radius);

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (Math.pow(i, 2) + Math.pow(j, 2) > Math.pow(radius, 2) + radius)         // (radius + .5)^2 =
                    // radius^2 + radius + .25
                    continue;
                BlockPos currentPos = reactorOrigin.offset(this.getRight(), i)
                        .offset(this.frontFacing.getOpposite(), j).offset(getUp(), heightTop);
                if (getWorld().getTileEntity(currentPos) instanceof IGregTechTileEntity gtTe) {
                    MetaTileEntity mte = gtTe.getMetaTileEntity();
                    if (mte instanceof ICoolantHandler coolantIn) {
                        Fluid lockedFluid = coolantIn.getLockedObject();
                        if (lockedFluid != null) {
                            ICoolantStats stats = CoolantRegistry.getCoolant(lockedFluid);
                            if (stats != null) {
                                continue;
                            }
                        }
                        this.unlockAll();
                        setLockingState(LockingState.MISSING_COOLANT);
                        return false;
                    } else if (mte instanceof IFuelRodHandler fuelIn) {
                        ItemStack lockedFuel = fuelIn.getInputStackHandler().getStackInSlot(0);
                        if (!lockedFuel.isEmpty()) {
                            IFissionFuelStats stats = FissionFuelRegistry.getFissionFuel(lockedFuel);
                            if (stats != null) {
                                foundFuel = true;
                                continue;
                            }
                        }
                        this.unlockAll();
                        setLockingState(LockingState.MISSING_FUEL);
                        return false;
                    }
                }
            }
        }
        if (!foundFuel) {
            this.unlockAll();
            setLockingState(LockingState.NO_FUEL_CHANNELS);
            return false;
        }
        return true;
    }

    private void addReactorComponents() {
        int radius = this.diameter / 2;     // This is the floor of the radius, the actual radius is 0.5 blocks
        // larger
        BlockPos.MutableBlockPos reactorOrigin = new BlockPos.MutableBlockPos(this.getPos());
        reactorOrigin.move(this.frontFacing.getOpposite(), radius);
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (Math.pow(i, 2) + Math.pow(j, 2) > Math.pow(radius, 2) + radius)         // (radius + .5)^2 =
                    // radius^2 + radius + .25
                    continue;
                BlockPos currentPos = reactorOrigin.offset(this.getRight(), i)
                        .offset(this.frontFacing.getOpposite(), j).offset(getUp(), heightTop);
                if (getWorld().getTileEntity(currentPos) instanceof IGregTechTileEntity gtTe) {
                    MetaTileEntity mte = gtTe.getMetaTileEntity();
                    if (mte instanceof ICoolantHandler coolantIn) {
                        Fluid lockedFluid = coolantIn.getLockedObject();
                        ICoolantStats stats = CoolantRegistry.getCoolant(lockedFluid);
                        coolantIn.setCoolant(stats);
                        coolantIn.getOutputHandler().setCoolant(stats);
                        CoolantChannel component = new CoolantChannel(100050, 0, stats, 1000, coolantIn,
                                coolantIn.getOutputHandler());
                        fissionReactor.addComponent(component, i + radius - 1, j + radius - 1);
                    } else if (mte instanceof IFuelRodHandler fuelIn) {
                        ItemStack lockedFuel = fuelIn.getInputStackHandler().getStackInSlot(0);
                        IFissionFuelStats stats = FissionFuelRegistry.getFissionFuel(lockedFuel);
                        FuelRod component;
                        fuelIn.setFuel(stats);
                        if (fuelIn.getDepletionPoint() == 0 || fuelIn.getPartialFuel() == null) {
                            fuelIn.setPartialFuel(stats);
                            component = new FuelRod(stats.getMaxTemperature(), 1, stats, 650);
                            fuelIn.getInputStackHandler().extractItem(0, 1, false); // Consume the fuel
                            fuelIn.markUndepleted(); // Set the depletion point
                        } else {
                            // It's guaranteed to have this property (if the implementation is correct).
                            IFissionFuelStats partialProp = fuelIn.getPartialFuel();
                            component = new FuelRod(partialProp.getMaxTemperature(), 1, partialProp, 650);
                        }
                        fuelIn.setInternalFuelRod(component);
                        fissionReactor.addComponent(component, i + radius - 1, j + radius - 1);
                    } else if (mte instanceof MetaTileEntityControlRodPort controlIn) {
                        ControlRod component = new ControlRod(100000, controlIn.hasModeratorTip(), 1, 800);
                        fissionReactor.addComponent(component, i + radius - 1, j + radius - 1);
                    } else if (mte instanceof MetaTileEntityModeratorPort moderatorIn) {
                        IModeratorStats moderator = moderatorIn.getModerator();
                        Moderator component = new Moderator(moderator, 0.5, 800);
                        fissionReactor.addComponent(component, i + radius - 1, j + radius - 1);
                    }
                }
            }
        }
    }

    private void updateReactorState() {
        this.fissionReactor.updatePower();
        this.fissionReactor.updateTemperature();
        this.fissionReactor.updatePressure();
        this.fissionReactor.updateNeutronPoisoning();
        this.fissionReactor.regulateControlRods();
    }

    protected void setLockingState(LockingState lockingState) {
        if (this.lockingState != lockingState) {
            writeCustomData(SCValues.SYNC_LOCKING_STATE, (buf) -> buf.writeEnumValue(lockingState));
        }
        this.lockingState = lockingState;
    }

    @Override
    public long getCoverCapacity() {
        // power is in MW
        return (long) (this.maxPower * 1e6);
    }

    @Override
    public long getCoverStored() {
        // power is in MW
        return (long) (this.power * 1e6);
    }

    public enum LockingState {
        // The reactor is locked
        LOCKED,
        // The reactor is unlocked
        UNLOCKED,
        // The reactor is supposed to be locked, but the locking logic is yet to run
        SHOULD_LOCK,
        // The reactor can't lock because it is missing fuel in a fuel channel
        MISSING_FUEL,
        // The reactor can't lock because it is missing coolant in a coolant channel
        MISSING_COOLANT,
        // The reactor can't lock because a fuel output is clogged
        FUEL_CLOGGED,
        // There are no fuel channels at all!
        NO_FUEL_CHANNELS,
        // The reactor can't lock because components are flagged as invalid
        INVALID_COMPONENT
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapes = new ArrayList<>();

        for (int diameter = 5; diameter <= 15; diameter += 2) {
            int radius = diameter % 2 == 0 ? (int) Math.floor(diameter / 2.f) :
                    Math.round((diameter - 1) / 2.f);
            StringBuilder interiorBuilder = new StringBuilder();

            String[] interiorSlice = new String[diameter];
            String[] controllerSlice;
            String[] topSlice;
            String[] bottomSlice;

            // First loop over the matrix
            for (int i = 0; i < diameter; i++) {
                for (int j = 0; j < diameter; j++) {
                    if (Math.pow(i - Math.floor(diameter / 2.), 2) +
                            Math.pow(j - Math.floor(diameter / 2.), 2) <
                            Math.pow(radius + 0.5f, 2)) {
                        interiorBuilder.append('A');
                    } else {
                        interiorBuilder.append(' ');
                    }
                }

                interiorSlice[i] = interiorBuilder.toString();
                interiorBuilder.setLength(0);
            }

            // Second loop is to detect where to put walls, the controller and I/O
            for (int i = 0; i < diameter; i++) {
                for (int j = 0; j < diameter; j++) {
                    if (interiorSlice[i].charAt(j) != 'A') {
                        continue;
                    }

                    int outerI = i + (int) Math.signum(i - (diameter / 2));

                    if (Math.pow(outerI - Math.floor(diameter / 2.), 2) +
                            Math.pow(j - Math.floor(diameter / 2.), 2) >
                            Math.pow(radius + 0.5f, 2)) {
                        interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'V');
                    }

                    int outerJ = j + (int) Math.signum(j - (diameter / 2));
                    if (Math.pow(i - Math.floor(diameter / 2.), 2) +
                            Math.pow(outerJ - Math.floor(diameter / 2.), 2) >
                            Math.pow(radius + 0.5f, 2)) {
                        interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'V');
                    }
                }
            }

            controllerSlice = interiorSlice.clone();
            topSlice = interiorSlice.clone();
            bottomSlice = interiorSlice.clone();
            controllerSlice[0] = controllerSlice[0].substring(0, (int) Math.floor(diameter / 2.)) + "SM" +
                    controllerSlice[0].substring((int) Math.floor(diameter / 2.) + 2);

            // Example hatches
            controllerSlice[1] = controllerSlice[1].substring(0, (int) Math.floor(diameter / 2.) - 1) + "fff" +
                    controllerSlice[1].substring((int) Math.floor(diameter / 2.) + 2);
            controllerSlice[2] = controllerSlice[2].substring(0, (int) Math.floor(diameter / 2.) - 1) + "fcf" +
                    controllerSlice[2].substring((int) Math.floor(diameter / 2.) + 2);
            controllerSlice[3] = controllerSlice[3].substring(0, (int) Math.floor(diameter / 2.) - 1) + "frf" +
                    controllerSlice[3].substring((int) Math.floor(diameter / 2.) + 2);

            topSlice[1] = topSlice[1].substring(0, (int) Math.floor(diameter / 2.) - 1) + "eee" +
                    topSlice[1].substring((int) Math.floor(diameter / 2.) + 2);
            topSlice[2] = topSlice[2].substring(0, (int) Math.floor(diameter / 2.) - 1) + "ebe" +
                    topSlice[2].substring((int) Math.floor(diameter / 2.) + 2);
            topSlice[3] = topSlice[3].substring(0, (int) Math.floor(diameter / 2.) - 1) + "eqe" +
                    topSlice[3].substring((int) Math.floor(diameter / 2.) + 2);

            bottomSlice[1] = bottomSlice[1].substring(0, (int) Math.floor(diameter / 2.) - 1) + "ggg" +
                    bottomSlice[1].substring((int) Math.floor(diameter / 2.) + 2);
            bottomSlice[2] = bottomSlice[2].substring(0, (int) Math.floor(diameter / 2.) - 1) + "gdg" +
                    bottomSlice[2].substring((int) Math.floor(diameter / 2.) + 2);
            bottomSlice[3] = bottomSlice[3].substring(0, (int) Math.floor(diameter / 2.) - 1) + "gVg" +
                    bottomSlice[3].substring((int) Math.floor(diameter / 2.) + 2);

            for (int i = 0; i < diameter; i++) {
                topSlice[i] = topSlice[i].replace('A', 'V');
                bottomSlice[i] = bottomSlice[i].replace('A', 'V');
            }
            DirectionalShapeInfoBuilder builder = new DirectionalShapeInfoBuilder(RelativeDirection.RIGHT,
                    RelativeDirection.FRONT, RelativeDirection.UP);
            builder.aisle(topSlice);
            for (int i = 0; i < heightBottom - 1; i++) {
                builder.aisle(interiorSlice);
            }
            builder.aisle(controllerSlice);
            for (int i = 0; i < heightTop - 1; i++) {
                builder.aisle(interiorSlice);
            }
            builder.aisle(bottomSlice);
            shapes.add(builder.where('S', SCMetaTileEntities.FISSION_REACTOR, EnumFacing.NORTH)
                    // A for interior components, which are air here
                    .where('A', Blocks.AIR.getDefaultState())
                    // Technically a duplicate, but this just makes things easier
                    .where(' ', Blocks.AIR.getDefaultState())
                    // I for the inputs on the top
                    .where('V', getVesselState())
                    .where('f', getFuelChannelState())
                    .where('c', getCoolantChannelState())
                    .where('r', getControlRodChannelState())
                    .where('e', SCMetaTileEntities.FUEL_ROD_INPUT, EnumFacing.UP)
                    .where('g', SCMetaTileEntities.FUEL_ROD_OUTPUT, EnumFacing.DOWN)
                    .where('b', SCMetaTileEntities.COOLANT_INPUT, EnumFacing.UP)
                    .where('d', SCMetaTileEntities.COOLANT_OUTPUT, EnumFacing.DOWN)
                    .where('q', SCMetaTileEntities.CONTROL_ROD, EnumFacing.UP)
                    .where('m', SCMetaTileEntities.CONTROL_ROD_MODERATED, EnumFacing.UP)

                    // B for the vessel blocks on the walls
                    .where('M', () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                            getVesselState(), EnumFacing.NORTH)
                    .build());
        }
        return shapes;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("supercritical.machine.fission_reactor.tooltip.1"));
        tooltip.add(I18n.format("supercritical.machine.fission_reactor.tooltip.2"));
        tooltip.add(I18n.format("supercritical.machine.fission_reactor.tooltip.3"));
    }

    @Override
    public boolean allowsExtendedFacing() {
        return SCConfigHolder.misc.allowExtendedFacingForFissionReactor;
    }
}
