package supercritical.common.metatileentities.multi;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import supercritical.api.capability.ICoolantHandler;
import supercritical.api.capability.IFuelRodHandler;
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch;
import supercritical.api.metatileentity.multiblock.SCMultiblockAbility;
import supercritical.api.nuclear.fission.CoolantRegistry;
import supercritical.api.nuclear.fission.FissionFuelRegistry;
import supercritical.api.nuclear.fission.FissionReactor;
import supercritical.api.nuclear.fission.IFissionFuelStats;
import supercritical.api.nuclear.fission.ModeratorRegistry;
import supercritical.api.nuclear.fission.components.ControlRod;
import supercritical.api.nuclear.fission.components.CoolantChannel;
import supercritical.api.nuclear.fission.components.FuelRod;
import supercritical.api.nuclear.fission.components.Moderator;
import supercritical.api.util.SCUtility;
import supercritical.api.cover.ICustomEnergyCover;
import supercritical.common.SCConfigHolder;
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityControlRodPort;
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityModeratorPort;
import supercritical.common.registry.SCBlocks;

import java.util.List;

public class MetaTileEntityFissionReactor extends MultiblockControllerMachine implements com.gregtechceu.gtceu.api.machine.feature.IUIMachine, ICustomEnergyCover {

    private FissionReactor reactor;
    @Persisted
    @DescSynced
    private boolean locked;
    @Persisted
    @DescSynced
    private LockingState lockingState = LockingState.UNLOCKED;
    private int diameter = 5;
    private int heightTop = 1;
    private int heightBottom = 1;
    private int reactorSize;
    private int reactorDepth;
    @Persisted
    @DescSynced
    private double controlRodInsertion = 1D;
    private TickableSubscription tickSubscription;
    @Persisted
    @DescSynced
    private boolean meltdown;
    @Persisted
    @DescSynced
    private boolean pressureExplosion;

    public MetaTileEntityFissionReactor(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (tickSubscription == null || !tickSubscription.isStillSubscribed()) {
            tickSubscription = subscribeServerTick(this::tickReactor);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    @Override
    public BlockPattern getPattern() {
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            this.heightTop = Math.clamp(findHeight(true), 1, 7);
            this.heightBottom = Math.clamp(findHeight(false), 1, 7);
            this.diameter = Math.clamp(findDiameter(), 5, 15) | 1;
        }
        return buildDynamicPattern();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        rebuildReactor();
        if (locked && !lockAndPrepareReactor()) {
            locked = false;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (locked) {
            unlockAll();
        }
    }

    private Direction getReactorUp() {
        return RelativeDirection.UP.getRelative(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    private Direction getReactorRight() {
        return RelativeDirection.RIGHT.getRelative(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    protected int findHeight(boolean top) {
        int i = 1;
        BlockPos.MutableBlockPos pos = getPos().mutable();
        Direction up = getReactorUp();
        Direction dir = top ? up : up.getOpposite();
        while (i <= 15) {
            if (isHeightEdge(getLevel(), pos, dir, i)) break;
            i++;
        }
        return i - 1;
    }

    protected int findDiameter() {
        int i = 1;
        BlockPos.MutableBlockPos pos = getPos().mutable();
        while (i <= 15) {
            pos.move(getFrontFacing().getOpposite());
            var state = getLevel().getBlockState(pos);
            if (state.getBlock() == SCBlocks.REACTOR_VESSEL.get()) {
                break;
            }
            var machine = MetaMachine.getMachine(getLevel(), pos);
            if (machine instanceof IFissionReactorHatch) {
                break;
            }
            if (PartAbility.MAINTENANCE.isApplicable(state.getBlock())) {
                break;
            }
            i++;
        }
        return i;
    }

    protected boolean isHeightEdge(Level level, BlockPos.MutableBlockPos pos, Direction direction, int steps) {
        pos.move(direction, steps);
        boolean edge;
        var state = level.getBlockState(pos);
        var block = state.getBlock();
        if (block == SCBlocks.REACTOR_VESSEL.get()) {
            edge = true;
        } else if (PartAbility.MAINTENANCE.isApplicable(block)) {
            edge = true;
        } else {
            var machine = MetaMachine.getMachine(level, pos);
            edge = machine instanceof IFissionReactorHatch;
        }
        pos.move(direction.getOpposite(), steps);
        return edge;
    }

    private TraceabilityPredicate moderatorPredicate() {
        return Predicates.custom(state -> ModeratorRegistry.getModerator(state.getBlockState().getBlock()) != null,
                () -> new BlockInfo[0]);
    }

    private BlockPattern buildDynamicPattern() {
        int radius = this.diameter % 2 == 0
                ? (int) Math.floor(this.diameter / 2.f)
                : Math.round((this.diameter - 1) / 2.f);

        StringBuilder interiorBuilder = new StringBuilder();
        String[] interiorSlice = new String[this.diameter];
        String[] controllerSlice;
        String[] topSlice;
        String[] bottomSlice;

        for (int i = 0; i < this.diameter; i++) {
            for (int j = 0; j < this.diameter; j++) {
                if (Math.pow(i - Math.floor(this.diameter / 2.), 2) +
                        Math.pow(j - Math.floor(this.diameter / 2.), 2) <
                        Math.pow(radius + 0.5f, 2)) {
                    interiorBuilder.append('A');
                } else {
                    interiorBuilder.append(' ');
                }
            }
            interiorSlice[i] = interiorBuilder.toString();
            interiorBuilder.setLength(0);
        }

        interiorSlice[this.diameter - 1] = interiorSlice[0] = interiorSlice[0].replace('A', 'B');
        for (int i = 1; i < this.diameter - 1; i++) {
            for (int j = 0; j < this.diameter; j++) {
                if (interiorSlice[i].charAt(j) != 'A') {
                    continue;
                }
                int outerI = i + (int) Math.signum(i - (this.diameter / 2));
                if (Math.pow(outerI - Math.floor(this.diameter / 2.), 2) +
                        Math.pow(j - Math.floor(this.diameter / 2.), 2) >
                        Math.pow(radius + 0.5f, 2)) {
                    interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'B');
                }
                int outerJ = j + (int) Math.signum(j - (this.diameter / 2));
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
                .where('S', Predicates.controller(Predicates.blocks(getDefinition().getBlock())))
                .where('A', Predicates.blocks(SCBlocks.FUEL_CHANNEL.get(), SCBlocks.CONTROL_ROD_CHANNEL.get(),
                        SCBlocks.COOLANT_CHANNEL.get())
                        .or(Predicates.air())
                        .or(moderatorPredicate())
                        .or(Predicates.abilities(SCMultiblockAbility.MODERATOR_PORT)))
                .where('I', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get()).or(getImportPredicate()))
                .where('O', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get())
                        .or(Predicates.abilities(SCMultiblockAbility.EXPORT_COOLANT, SCMultiblockAbility.EXPORT_FUEL_ROD)))
                .where('B', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get())
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1).setMaxGlobalLimited(1)))
                .where(' ', Predicates.any())
                .build();
    }

    public static BlockPattern buildPattern(MultiblockMachineDefinition definition, int diameter, int heightBottom, int heightTop) {
        int radius = diameter % 2 == 0
                ? (int) Math.floor(diameter / 2.f)
                : Math.round((diameter - 1) / 2.f);

        StringBuilder interiorBuilder = new StringBuilder();
        String[] interiorSlice = new String[diameter];

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

        interiorSlice[diameter - 1] = interiorSlice[0] = interiorSlice[0].replace('A', 'B');
        for (int i = 1; i < diameter - 1; i++) {
            for (int j = 0; j < diameter; j++) {
                if (interiorSlice[i].charAt(j) != 'A') {
                    continue;
                }
                int outerI = i + (int) Math.signum(i - (diameter / 2));
                if (Math.pow(outerI - Math.floor(diameter / 2.), 2) +
                        Math.pow(j - Math.floor(diameter / 2.), 2) >
                        Math.pow(radius + 0.5f, 2)) {
                    interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'B');
                }
                int outerJ = j + (int) Math.signum(j - (diameter / 2));
                if (Math.pow(i - Math.floor(diameter / 2.), 2) +
                        Math.pow(outerJ - Math.floor(diameter / 2.), 2) >
                        Math.pow(radius + 0.5f, 2)) {
                    interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'B');
                }
            }
        }

        String[] controllerSlice = interiorSlice.clone();
        String[] topSlice = interiorSlice.clone();
        String[] bottomSlice = interiorSlice.clone();
        controllerSlice[0] = controllerSlice[0].substring(0, (int) Math.floor(diameter / 2.)) + 'S' +
                controllerSlice[0].substring((int) Math.floor(diameter / 2.) + 1);
        for (int i = 0; i < diameter; i++) {
            topSlice[i] = topSlice[i].replace('A', 'I');
            bottomSlice[i] = bottomSlice[i].replace('A', 'O');
        }

        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
                .aisle(bottomSlice)
                .aisle(interiorSlice).setRepeatable(heightBottom - 1)
                .aisle(controllerSlice)
                .aisle(interiorSlice).setRepeatable(heightTop - 1)
                .aisle(topSlice)
                .where('S', Predicates.controller(Predicates.blocks(definition.getBlock())))
                .where('A', Predicates.blocks(SCBlocks.FUEL_CHANNEL.get(), SCBlocks.CONTROL_ROD_CHANNEL.get(),
                        SCBlocks.COOLANT_CHANNEL.get())
                        .or(Predicates.air()))
                .where('I', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get())
                        .or(Predicates.abilities(SCMultiblockAbility.IMPORT_COOLANT, SCMultiblockAbility.IMPORT_FUEL_ROD,
                                SCMultiblockAbility.CONTROL_ROD_PORT, SCMultiblockAbility.MODERATOR_PORT)))
                .where('O', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get())
                        .or(Predicates.abilities(SCMultiblockAbility.EXPORT_COOLANT, SCMultiblockAbility.EXPORT_FUEL_ROD)))
                .where('B', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get())
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1).setMaxGlobalLimited(1)))
                .where(' ', Predicates.any())
                .build();
    }

    private TraceabilityPredicate getImportPredicate() {
        PartAbility[] allowedAbilities = {
                SCMultiblockAbility.IMPORT_COOLANT,
                SCMultiblockAbility.IMPORT_FUEL_ROD,
                SCMultiblockAbility.CONTROL_ROD_PORT,
                SCMultiblockAbility.MODERATOR_PORT
        };
        return Predicates.custom(state -> {
            var machine = MetaMachine.getMachine(state.getWorld(), state.getPos());
            if (!(machine instanceof IFissionReactorHatch hatch)) {
                state.setError(new PatternStringError(
                        "supercritical.multiblock.pattern.error.hatch_invalid"));
                return false;
            }
            Block block = state.getBlockState().getBlock();
            boolean allowed = false;
            for (PartAbility ability : allowedAbilities) {
                if (ability.isApplicable(block)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                state.setError(new PatternStringError(
                        "supercritical.multiblock.pattern.error.hatch_invalid"));
                return false;
            }
            if (!hatch.checkValidity(getHeight() - 1)) {
                state.setError(new PatternStringError(
                        "supercritical.multiblock.pattern.error.hatch_invalid"));
                return false;
            }
            return true;
        }, () -> new BlockInfo[0]);
    }

    private void lockAll() {
        for (ICoolantHandler handler : getCoolantHandlers()) {
            handler.setLock(true);
        }
        for (IFuelRodHandler handler : getFuelRodHandlers()) {
            handler.setLock(true);
        }
    }

    private void unlockAll() {
        if (reactor != null) {
            double depletion = reactor.fuelDepletion;
            for (IFuelRodHandler handler : getFuelRodHandlers()) {
                handler.resetDepletion(depletion);
                handler.setLock(false);
            }
        } else {
            for (IFuelRodHandler handler : getFuelRodHandlers()) {
                handler.setLock(false);
            }
        }
        for (ICoolantHandler handler : getCoolantHandlers()) {
            handler.setLock(false);
        }
        if (reactor != null) {
            reactor.setOn(false);
            reactor.resetFuelDepletion();
        }
    }

    private boolean lockAndPrepareReactor() {
        if (!verifyCorrectness()) {
            this.locked = false;
            return false;
        }
        this.lockAll();
        this.addReactorComponents();
        reactor.prepareThermalProperties();
        reactor.computeGeometry();
        setLockingState(LockingState.LOCKED);
        return true;
    }

    private boolean verifyCorrectness() {
        boolean foundFuel = false;
        for (var part : getParts()) {
            if (part instanceof ICoolantHandler coolantIn) {
                var lockedFluid = coolantIn.getLockedObject();
                if (lockedFluid != null) {
                    var stats = CoolantRegistry.getCoolant(lockedFluid);
                    if (coolantIn.getOutputHandler() == null && !coolantIn.checkValidity(getHeight() - 1)) {
                        setLockingState(LockingState.INVALID_COMPONENT);
                        return false;
                    }
                    if (stats != null) {
                        continue;
                    }
                }
                this.unlockAll();
                setLockingState(LockingState.MISSING_COOLANT);
                return false;
            } else if (part instanceof IFuelRodHandler fuelIn) {
                ItemStack lockedFuel = fuelIn.getInputStackHandler().getStackInSlot(0);
                if (!lockedFuel.isEmpty()) {
                    IFissionFuelStats stats = FissionFuelRegistry.getFissionFuel(lockedFuel);
                    if (stats != null) {
                        foundFuel = true;
                        continue;
                    }
                } else if (fuelIn.getPartialFuel() != null) {
                    foundFuel = true;
                    continue;
                }
                this.unlockAll();
                setLockingState(LockingState.MISSING_FUEL);
                return false;
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
        if (reactor == null || getLevel() == null) {
            return;
        }
        reactor.turnOff();
        int radius = this.diameter / 2;
        int size = this.diameter - 2;
        BlockPos.MutableBlockPos reactorOrigin = getPos().mutable();
        reactorOrigin.move(getFrontFacing().getOpposite(), radius);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int i = x - (radius - 1);
                int j = y - (radius - 1);
                if (Math.pow(i, 2) + Math.pow(j, 2) > Math.pow(radius, 2) + radius) {
                    continue;
                }
                BlockPos currentPos = reactorOrigin.mutable().move(getReactorRight(), i)
                        .move(getFrontFacing().getOpposite(), j)
                        .move(getReactorUp(), heightTop)
                        .immutable();
                var machine = MetaMachine.getMachine(getLevel(), currentPos);
                if (machine instanceof ICoolantHandler coolantIn) {
                    var lockedFluid = coolantIn.getLockedObject();
                    var stats = CoolantRegistry.getCoolant(lockedFluid);
                    coolantIn.setCoolant(stats);
                    if (coolantIn.getOutputHandler() != null) {
                        coolantIn.getOutputHandler().setCoolant(stats);
                    }
                    var component = new CoolantChannel(100050, 0, stats, 1000);
                    component.setHandlers(coolantIn, coolantIn.getOutputHandler());
                    reactor.setComponent(x, y, component);
                } else if (machine instanceof IFuelRodHandler fuelIn) {
                    ItemStack lockedFuel = fuelIn.getInputStackHandler().getStackInSlot(0);
                    IFissionFuelStats stats = FissionFuelRegistry.getFissionFuel(lockedFuel);
                    FuelRod component;
                    fuelIn.setFuel(stats);
                    if (fuelIn.getDepletionPoint() == 0 || fuelIn.getPartialFuel() == null) {
                        fuelIn.setPartialFuel(stats);
                        component = new FuelRod(stats.getMaxTemperature(), 1, stats, 650);
                        fuelIn.getInputStackHandler().extractItem(0, 1, false);
                        fuelIn.markUndepleted();
                    } else {
                        IFissionFuelStats partialProp = fuelIn.getPartialFuel();
                        component = new FuelRod(partialProp.getMaxTemperature(), 1, partialProp, 650);
                    }
                    fuelIn.setInternalFuelRod(component);
                    reactor.setComponent(x, y, component);
                } else if (machine instanceof MetaTileEntityControlRodPort controlIn) {
                    var component = new ControlRod(100000, controlIn.hasModeratorTip(), 1, 800);
                    reactor.setComponent(x, y, component);
                } else if (machine instanceof MetaTileEntityModeratorPort moderatorIn) {
                    var moderator = moderatorIn.getModerator();
                    var component = new Moderator(0.5, 800, moderator);
                    reactor.setComponent(x, y, component);
                }
            }
        }
    }

    private List<ICoolantHandler> getCoolantHandlers() {
        return getParts().stream()
                .filter(part -> part instanceof ICoolantHandler)
                .map(part -> (ICoolantHandler) part)
                .toList();
    }

    public void rebuildReactor() {
        int size = Math.max(3, (diameter - 2) | 1);
        int depth = Math.max(1, getHeight() - 2);
        if (reactor != null && reactorSize == size && reactorDepth == depth) {
            return;
        }
        reactorSize = size;
        reactorDepth = depth;
        FissionReactor old = reactor;
        reactor = new FissionReactor(size, depth, controlRodInsertion);
        if (old != null) {
            reactor.setOn(old.isOn());
            reactor.kEff = old.kEff;
            reactor.power = old.power;
            reactor.temperature = old.temperature;
            reactor.pressure = old.pressure;
            reactor.fuelDepletion = old.fuelDepletion;
            reactor.accumulatedHydrogen = old.accumulatedHydrogen;
        }
        reactor.prepareThermalProperties();
        reactor.computeGeometry();
    }

    public void tickReactor() {
        if (reactor == null || !locked || !isFormed() || meltdown || pressureExplosion) return;
        reactor.setOn(true);
        reactor.controlRodInsertion = controlRodInsertion;
        reactor.tick();
        if (getOffsetTimer() % 20 == 0) {
            handleFuel();
        }
        checkFailureState();
    }

    private void handleFuel() {
        boolean canWork = true;
        for (IFuelRodHandler fuelImport : getFuelRodHandlers()) {
            if (fuelImport.isDepleted(reactor.fuelDepletion)) {
                var output = fuelImport.getOutputStackHandler(getHeight() - 1);
                if (output == null || !output.insertItem(0, fuelImport.getDepletedFuel(), true).isEmpty()) {
                    canWork = false;
                    setLockingState(LockingState.FUEL_CLOGGED);
                    setLocked(false);
                    break;
                }
                output.insertItem(0, fuelImport.getDepletedFuel(), false);
                fuelImport.markUndepleted();
                var input = fuelImport.getInputStackHandler();
                if (input.extractItem(0, 1, true).isEmpty()) {
                    canWork = false;
                    fuelImport.setPartialFuel(null);
                    setLockingState(LockingState.MISSING_FUEL);
                    setLocked(false);
                    break;
                }
                input.extractItem(0, 1, false);
            }
        }
        if (!canWork) {
            reactor.setOn(false);
        }
    }

    public int getHeight() {
        return heightTop + heightBottom + 1;
    }

    private List<IFuelRodHandler> getFuelRodHandlers() {
        return getParts().stream()
                .filter(part -> part instanceof IFuelRodHandler)
                .map(part -> (IFuelRodHandler) part)
                .toList();
    }

    private void checkFailureState() {
        if (reactor == null) return;
        if (SCConfigHolder.NUCLEAR.enableMeltdown.get() && reactor.temperature >= reactor.maxTemperature) {
            meltdown = true;
            locked = false;
            reactor.setOn(false);
            markDirty();
        }
        if (reactor.pressure >= reactor.maxPressure) {
            pressureExplosion = true;
            locked = false;
            reactor.setOn(false);
            markDirty();
        }
    }

    public void addDisplayText(List<Component> text) {
        text.add(Component.translatable("supercritical.gui.fission.lock." + lockingState.name().toLowerCase()));
        text.add(Component.translatable("supercritical.multiblock.fission_reactor.diameter", diameter));
        text.add(Component.translatable("supercritical.multiblock.fission_reactor.height", heightTop + heightBottom + 1));
        text.add(Component.translatable("supercritical.gui.fission.control_rod_insertion", Math.round(controlRodInsertion * 100D)));
        if (meltdown) {
            text.add(Component.translatable("supercritical.multiblock.fission_reactor.meltdown"));
        }
        if (pressureExplosion) {
            text.add(Component.translatable("supercritical.multiblock.fission_reactor.pressure_explosion"));
        }
        if (reactor != null) {
            text.add(Component.translatable("supercritical.gui.fission.temperature", reactor.temperature));
            text.add(Component.translatable("supercritical.gui.fission.pressure", reactor.pressure));
            text.add(Component.translatable("supercritical.gui.fission.power", reactor.power, reactor.maxPower));
            text.add(Component.translatable("supercritical.gui.fission.k_eff", reactor.kEff));
        }
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putBoolean("Locked", locked);
        tag.putString("LockingState", lockingState.name());
        tag.putInt("Diameter", diameter);
        tag.putInt("HeightTop", heightTop);
        tag.putInt("HeightBottom", heightBottom);
        tag.putDouble("ControlRodInsertion", controlRodInsertion);
        tag.putBoolean("Meltdown", meltdown);
        tag.putBoolean("PressureExplosion", pressureExplosion);
        if (reactor != null) {
            tag.putBoolean("ReactorOn", reactor.isOn());
            tag.putDouble("KEff", reactor.kEff);
            tag.putDouble("Power", reactor.power);
            tag.putDouble("Temperature", reactor.temperature);
            tag.putDouble("Pressure", reactor.pressure);
            tag.putDouble("FuelDepletion", reactor.fuelDepletion);
            tag.putDouble("AccumulatedHydrogen", reactor.accumulatedHydrogen);
        }
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        locked = tag.getBoolean("Locked");
        if (tag.contains("LockingState")) {
            try {
                lockingState = LockingState.valueOf(tag.getString("LockingState"));
            } catch (IllegalArgumentException ignored) {
                lockingState = locked ? LockingState.LOCKED : LockingState.UNLOCKED;
            }
        } else {
            lockingState = locked ? LockingState.LOCKED : LockingState.UNLOCKED;
        }
        diameter = tag.contains("Diameter") ? tag.getInt("Diameter") : 5;
        heightTop = tag.contains("HeightTop") ? tag.getInt("HeightTop") : 1;
        heightBottom = tag.contains("HeightBottom") ? tag.getInt("HeightBottom") : 1;
        controlRodInsertion = tag.contains("ControlRodInsertion") ? tag.getDouble("ControlRodInsertion") : 1D;
        meltdown = tag.getBoolean("Meltdown");
        pressureExplosion = tag.getBoolean("PressureExplosion");
        rebuildReactor();
        if (reactor != null) {
            reactor.setOn(tag.getBoolean("ReactorOn"));
            reactor.kEff = tag.getDouble("KEff");
            reactor.power = tag.getDouble("Power");
            reactor.temperature = tag.contains("Temperature") ? tag.getDouble("Temperature") : FissionReactor.ROOM_TEMPERATURE;
            reactor.pressure = tag.contains("Pressure") ? tag.getDouble("Pressure") : FissionReactor.STANDARD_PRESSURE;
            reactor.fuelDepletion = tag.contains("FuelDepletion") ? tag.getDouble("FuelDepletion") : -1;
            reactor.accumulatedHydrogen = tag.getDouble("AccumulatedHydrogen");
        }
    }

    public boolean canToggle() {
        return isFormed() && !meltdown && !pressureExplosion && (reactor != null || !locked)
                && (!locked || SCConfigHolder.NUCLEAR.enableMeltdown.get() || reactor.temperature < reactor.maxTemperature);
    }

    public void setLocked(boolean locked) {
        if (!canToggle() && locked) return;
        this.locked = locked;
        if (locked) {
            setLockingState(LockingState.SHOULD_LOCK);
            if (!lockAndPrepareReactor()) {
                if (reactor != null) {
                    reactor.setOn(false);
                }
                return;
            }
        } else {
            unlockAll();
            if (lockingState == LockingState.LOCKED || lockingState == LockingState.SHOULD_LOCK) {
                setLockingState(LockingState.UNLOCKED);
            }
        }
        if (reactor != null) {
            reactor.setOn(locked);
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void resetFailureState() {
        meltdown = false;
        pressureExplosion = false;
        locked = false;
        setLockingState(LockingState.UNLOCKED);
        if (reactor != null) {
            reactor.setOn(false);
            reactor.temperature = FissionReactor.ROOM_TEMPERATURE;
            reactor.pressure = FissionReactor.STANDARD_PRESSURE;
            reactor.power = 0D;
        }
        markDirty();
    }

    public boolean hasMeltdown() {
        return meltdown;
    }

    public boolean hasPressureExplosion() {
        return pressureExplosion;
    }

    public FissionReactor getReactor() {
        return reactor;
    }

    public boolean hasReactor() {
        return reactor != null;
    }

    public int getDiameter() {
        return diameter;
    }

    public void setDiameter(int diameter) {
        this.diameter = Math.max(5, Math.min(15, diameter | 1));
        rebuildReactor();
    }

    public int getHeightTop() {
        return heightTop;
    }

    public void setHeightTop(int heightTop) {
        this.heightTop = Math.max(1, Math.min(7, heightTop));
        rebuildReactor();
    }

    public int getHeightBottom() {
        return heightBottom;
    }

    public void setHeightBottom(int heightBottom) {
        this.heightBottom = Math.max(1, Math.min(7, heightBottom));
        rebuildReactor();
    }

    public double getControlRodInsertion() {
        return controlRodInsertion;
    }

    public void setControlRodInsertion(double controlRodInsertion) {
        this.controlRodInsertion = Math.max(0D, Math.min(1D, controlRodInsertion));
        if (reactor != null) {
            reactor.updateControlRodInsertion(this.controlRodInsertion);
        }
    }

    @Override
    public long getCoverCapacity() {
        return reactor == null ? 0L : (long) (reactor.maxPower * 1e6);
    }

    @Override
    public long getCoverStored() {
        return reactor == null ? 0L : (long) (reactor.power * 1e6);
    }

    private double getHeatFillPercentage() {
        return reactor == null || reactor.maxTemperature <= 0D ? 0D : Math.min(1D, reactor.temperature / reactor.maxTemperature);
    }

    private double getPressureFillPercentage() {
        return reactor == null || reactor.maxPressure <= 0D ? 0D : Math.min(1D, reactor.pressure / reactor.maxPressure);
    }

    private double getPowerFillPercentage() {
        return reactor == null || reactor.maxPower <= 0D ? 0D : Math.min(1D, reactor.power / reactor.maxPower);
    }

    private void setLockingState(LockingState lockingState) {
        this.lockingState = lockingState;
        markDirty();
    }

    public enum LockingState {
        LOCKED,
        UNLOCKED,
        SHOULD_LOCK,
        MISSING_FUEL,
        MISSING_COOLANT,
        FUEL_CLOGGED,
        NO_FUEL_CHANNELS,
        INVALID_COMPONENT
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 226, 109).setBackground(GuiTextures.DISPLAY);
        screen.addWidget(new LabelWidget(4, 5, self().getDefinition().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .textSupplier(self().getLevel().isClientSide ? null : this::addDisplayText)
                .setMaxWidthLimit(216));

        return new ModularUI(240, 208, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(screen)
                .widget(new ProgressWidget(this::getHeatFillPercentage, 4, 115, 76, 7,
                        GuiTextures.PROGRESS_BAR_BOILER_HEAT)
                        .setHoverTooltips(Component.translatable("supercritical.gui.fission.temperature",
                                reactor == null ? 0D : reactor.temperature).getString()))
                .widget(new ProgressWidget(this::getPressureFillPercentage, 82, 115, 76, 7,
                        GuiTextures.PROGRESS_BAR_COMPRESS)
                        .setHoverTooltips(Component.translatable("supercritical.gui.fission.pressure",
                                reactor == null ? 0D : reactor.pressure).getString()))
                .widget(new ProgressWidget(this::getPowerFillPercentage, 160, 115, 76, 7,
                        GuiTextures.PROGRESS_BAR_ARROW)
                        .setHoverTooltips(Component.translatable("supercritical.gui.fission.power",
                                reactor == null ? 0D : reactor.power, reactor == null ? 0D : reactor.maxPower).getString()))
                .widget(new LabelWidget(10, 132, () -> Component.translatable(
                        "supercritical.gui.fission.control_rod_insertion",
                        String.format("%.2f%%", controlRodInsertion * 100D)).getString()))
                .widget(new ButtonWidget(10, 146, 20, 18,
                        new TextTexture("-"), cd -> {
                    if (!cd.isRemote) {
                        setControlRodInsertion(controlRodInsertion - 0.01D);
                    }
                }).setHoverTooltips("gui.widget.incrementButton.default_tooltip"))
                .widget(new ButtonWidget(34, 146, 20, 18,
                        new TextTexture("+"), cd -> {
                    if (!cd.isRemote) {
                        setControlRodInsertion(controlRodInsertion + 0.01D);
                    }
                }).setHoverTooltips("gui.widget.incrementButton.default_tooltip"))
                .widget(new ToggleButtonWidget(215, 125, 18, 18,
                        GuiTextures.BUTTON_WORKING_ENABLE, () -> reactor != null && reactor.controlRodRegulationOn,
                        enabled -> {
                            if (reactor != null) {
                                reactor.controlRodRegulationOn = enabled;
                            }
                        })
                        .setShouldUseBaseBackground()
                        .setTooltipText("supercritical.gui.fission.helper"))
                .widget(new ToggleButtonWidget(215, 183, 18, 18,
                        GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)
                        .setShouldUseBaseBackground()
                        .setTooltipText("supercritical.gui.fission.lock"))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 125, true));
    }
}
