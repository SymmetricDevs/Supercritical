package supercritical.common.metatileentities.multi

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.UITemplate
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.pattern.*
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.gui.texture.TextTexture
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.*
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.utils.BlockInfo
import it.unimi.dsi.fastutil.booleans.BooleanConsumer
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import supercritical.api.capability.ICoolantHandler
import supercritical.api.capability.IFuelRodHandler
import supercritical.api.cover.ICustomEnergyCover
import supercritical.api.metatileentity.multiblock.IFissionReactorHatch
import supercritical.api.metatileentity.multiblock.SCMultiblockAbility
import supercritical.api.nuclear.fission.CoolantRegistry
import supercritical.api.nuclear.fission.FissionFuelRegistry
import supercritical.api.nuclear.fission.FissionReactor
import supercritical.api.nuclear.fission.ModeratorRegistry
import supercritical.api.nuclear.fission.components.ControlRod
import supercritical.api.nuclear.fission.components.CoolantChannel
import supercritical.api.nuclear.fission.components.FuelRod
import supercritical.api.nuclear.fission.components.Moderator
import supercritical.api.util.SCUtility
import supercritical.common.SCConfigHolder
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityControlRodPort
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityModeratorPort
import supercritical.common.registry.SCBlocks
import java.util.*
import java.util.function.*
import kotlin.math.*

class MetaTileEntityFissionReactor(holder: IMachineBlockEntity) : MultiblockControllerMachine(holder), IUIMachine,
    ICustomEnergyCover {
    var reactor: FissionReactor? = null
        private set

    @Persisted
    @DescSynced
    private var locked = false

    @Persisted
    @DescSynced
    private var lockingState = LockingState.UNLOCKED
    private var diameter = 5
    private var heightTop = 1
    private var heightBottom = 1
    private var reactorSize = 0
    private var reactorDepth = 0

    @Persisted
    @DescSynced
    private var controlRodInsertion = 1.0
    private var tickSubscription: TickableSubscription? = null

    @Persisted
    @DescSynced
    private var meltdown = false

    @Persisted
    @DescSynced
    private var pressureExplosion = false

    override fun onLoad() {
        super.onLoad()
        if (tickSubscription == null || !tickSubscription!!.isStillSubscribed()) {
            tickSubscription = subscribeServerTick(Runnable { this.tickReactor() })
        }
    }

    override fun onUnload() {
        super.onUnload()
        if (tickSubscription != null) {
            tickSubscription!!.unsubscribe()
            tickSubscription = null
        }
    }

    override fun getPattern(): BlockPattern? {
        val level = getLevel()
        if (level != null && !level.isClientSide) {
            this.heightTop = Math.clamp(findHeight(true).toLong(), 1, 7)
            this.heightBottom = Math.clamp(findHeight(false).toLong(), 1, 7)
            this.diameter = Math.clamp(findDiameter().toLong(), 5, 15) or 1
        }
        return buildDynamicPattern()
    }

    override fun onStructureFormed() {
        super.onStructureFormed()
        rebuildReactor()
        if (locked && !lockAndPrepareReactor()) {
            locked = false
        }
    }

    override fun onStructureInvalid() {
        super.onStructureInvalid()
        if (locked) {
            unlockAll()
        }
    }

    private val reactorUp: Direction
        get() = RelativeDirection.UP.getRelative(getFrontFacing(), getUpwardsFacing(), isFlipped())

    private val reactorRight: Direction
        get() = RelativeDirection.RIGHT.getRelative(getFrontFacing(), getUpwardsFacing(), isFlipped())

    protected fun findHeight(top: Boolean): Int {
        var i = 1
        val pos = getPos().mutable()
        val up = this.reactorUp
        val dir = if (top) up else up.getOpposite()
        while (i <= 15) {
            if (isHeightEdge(getLevel()!!, pos, dir, i)) break
            i++
        }
        return i - 1
    }

    protected fun findDiameter(): Int {
        var i = 1
        val pos = getPos().mutable()
        while (i <= 15) {
            pos.move(getFrontFacing().getOpposite())
            val state = getLevel()!!.getBlockState(pos)
            if (state.getBlock() === SCBlocks.REACTOR_VESSEL.get()) {
                break
            }
            val machine = getMachine(getLevel(), pos)
            if (machine is IFissionReactorHatch) {
                break
            }
            if (PartAbility.MAINTENANCE.isApplicable(state.getBlock())) {
                break
            }
            i++
        }
        return i
    }

    protected fun isHeightEdge(level: Level, pos: MutableBlockPos, direction: Direction, steps: Int): Boolean {
        pos.move(direction, steps)
        val edge: Boolean
        val state = level.getBlockState(pos)
        val block = state.getBlock()
        if (block === SCBlocks.REACTOR_VESSEL.get()) {
            edge = true
        } else if (PartAbility.MAINTENANCE.isApplicable(block)) {
            edge = true
        } else {
            val machine = getMachine(level, pos)
            edge = machine is IFissionReactorHatch
        }
        pos.move(direction.getOpposite(), steps)
        return edge
    }

    private fun moderatorPredicate(): TraceabilityPredicate {
        return Predicates.custom(
            Predicate { state: MultiblockState? ->
                ModeratorRegistry.getModerator(
                    state!!.getBlockState().getBlock()
                ) != null
            },
            Supplier { arrayOfNulls<BlockInfo>(0) })
    }

    private fun buildDynamicPattern(): BlockPattern? {
        val radius = if (this.diameter % 2 == 0) floor((this.diameter / 2f).toDouble()).toInt() else
            Math.round((this.diameter - 1) / 2f)

        val interiorBuilder = StringBuilder()
        val interiorSlice = arrayOfNulls<String>(this.diameter)
        val controllerSlice: Array<String?>
        val topSlice: Array<String?>
        val bottomSlice: Array<String?>

        for (i in 0..<this.diameter) {
            for (j in 0..<this.diameter) {
                if ((i - floor(this.diameter / 2.0)).pow(2.0) + (j - floor(this.diameter / 2.0)).pow(2.0) < (radius + 0.5f).toDouble()
                        .pow(2.0)
                ) {
                    interiorBuilder.append('A')
                } else {
                    interiorBuilder.append(' ')
                }
            }
            interiorSlice[i] = interiorBuilder.toString()
            interiorBuilder.setLength(0)
        }

        interiorSlice[0] = interiorSlice[0]!!.replace('A', 'B')
        interiorSlice[this.diameter - 1] = interiorSlice[0]
        for (i in 1..<this.diameter - 1) {
            for (j in 0..<this.diameter) {
                if (interiorSlice[i]!!.get(j) != 'A') {
                    continue
                }
                val outerI = i + sign((i - (this.diameter / 2)).toFloat()).toInt()
                if ((outerI - floor(this.diameter / 2.0)).pow(2.0) + (j - floor(this.diameter / 2.0)).pow(2.0) > (radius + 0.5f).toDouble()
                        .pow(2.0)
                ) {
                    interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'B')
                }
                val outerJ = j + sign((j - (this.diameter / 2)).toFloat()).toInt()
                if ((i - floor(this.diameter / 2.0)).pow(2.0) + (outerJ - floor(this.diameter / 2.0)).pow(2.0) > (radius + 0.5f).toDouble()
                        .pow(2.0)
                ) {
                    interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'B')
                }
            }
        }

        controllerSlice = interiorSlice.clone()
        topSlice = interiorSlice.clone()
        bottomSlice = interiorSlice.clone()
        controllerSlice[0] = controllerSlice[0]!!.substring(0, floor(this.diameter / 2.0).toInt()) + 'S' +
                controllerSlice[0]!!.substring(floor(this.diameter / 2.0).toInt() + 1)
        for (i in 0..<this.diameter) {
            topSlice[i] = topSlice[i]!!.replace('A', 'I')
            bottomSlice[i] = bottomSlice[i]!!.replace('A', 'O')
        }

        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
            .aisle(*bottomSlice)
            .aisle(*interiorSlice).setRepeatable(heightBottom - 1)
            .aisle(*controllerSlice)
            .aisle(*interiorSlice).setRepeatable(heightTop - 1)
            .aisle(*topSlice)
            .where('S', Predicates.controller(Predicates.blocks(getDefinition().getBlock())))
            .where(
                'A', Predicates.blocks(
                    SCBlocks.FUEL_CHANNEL.get(), SCBlocks.CONTROL_ROD_CHANNEL.get(),
                    SCBlocks.COOLANT_CHANNEL.get()
                )
                    .or(Predicates.air())
                    .or(moderatorPredicate())
                    .or(Predicates.abilities(SCMultiblockAbility.MODERATOR_PORT))
            )
            .where('I', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get()).or(this.importPredicate))
            .where(
                'O', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get())
                    .or(Predicates.abilities(SCMultiblockAbility.EXPORT_COOLANT, SCMultiblockAbility.EXPORT_FUEL_ROD))
            )
            .where(
                'B', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get())
                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1).setMaxGlobalLimited(1))
            )
            .where(' ', Predicates.any())
            .build()
    }

    private val importPredicate: TraceabilityPredicate
        get() {
            val allowedAbilities = arrayOf<PartAbility?>(
                SCMultiblockAbility.IMPORT_COOLANT,
                SCMultiblockAbility.IMPORT_FUEL_ROD,
                SCMultiblockAbility.CONTROL_ROD_PORT,
                SCMultiblockAbility.MODERATOR_PORT
            )
            return Predicates.custom(Predicate { state: MultiblockState? ->
                val machine = getMachine(state!!.getWorld(), state.getPos())
                if (machine !is IFissionReactorHatch) {
                    state.setError(
                        PatternStringError(
                            "supercritical.multiblock.pattern.error.hatch_invalid"
                        )
                    )
                    return@custom false
                }
                val block = state.getBlockState().getBlock()
                var allowed = false
                for (ability in allowedAbilities) {
                    if (ability!!.isApplicable(block)) {
                        allowed = true
                        break
                    }
                }
                if (!allowed) {
                    state.setError(
                        PatternStringError(
                            "supercritical.multiblock.pattern.error.hatch_invalid"
                        )
                    )
                    return@custom false
                }
                if (!machine.checkValidity(this.height - 1)) {
                    state.setError(
                        PatternStringError(
                            "supercritical.multiblock.pattern.error.hatch_invalid"
                        )
                    )
                    return@custom false
                }
                true
            }, Supplier { arrayOfNulls<BlockInfo>(0) })
        }

    private fun lockAll() {
        for (handler in this.coolantHandlers) {
            handler.setLock(true)
        }
        for (handler in this.fuelRodHandlers) {
            handler.setLock(true)
        }
    }

    private fun unlockAll() {
        if (reactor != null) {
            val depletion = reactor!!.fuelDepletion
            for (handler in this.fuelRodHandlers) {
                handler.resetDepletion(depletion)
                handler.setLock(false)
            }
        } else {
            for (handler in this.fuelRodHandlers) {
                handler.setLock(false)
            }
        }
        for (handler in this.coolantHandlers) {
            handler.setLock(false)
        }
        if (reactor != null) {
            reactor!!.setOn(false)
            reactor!!.resetFuelDepletion()
        }
    }

    private fun lockAndPrepareReactor(): Boolean {
        if (!verifyCorrectness()) {
            this.locked = false
            return false
        }
        this.lockAll()
        this.addReactorComponents()
        reactor!!.prepareThermalProperties()
        reactor!!.computeGeometry()
        setLockingState(LockingState.LOCKED)
        return true
    }

    private fun verifyCorrectness(): Boolean {
        var foundFuel = false
        for (part in getParts()) {
            if (part is ICoolantHandler) {
                val lockedFluid = part.getLockedObject()
                if (lockedFluid != null) {
                    val stats = CoolantRegistry.getCoolant(lockedFluid)
                    if (part.getOutputHandler() == null && !part.checkValidity(this.height - 1)) {
                        setLockingState(LockingState.INVALID_COMPONENT)
                        return false
                    }
                    if (stats != null) {
                        continue
                    }
                }
                this.unlockAll()
                setLockingState(LockingState.MISSING_COOLANT)
                return false
            } else if (part is IFuelRodHandler) {
                val lockedFuel = part.getInputStackHandler().getStackInSlot(0)
                if (!lockedFuel.isEmpty()) {
                    val stats = FissionFuelRegistry.getFissionFuel(lockedFuel)
                    if (stats != null) {
                        foundFuel = true
                        continue
                    }
                } else if (part.getPartialFuel() != null) {
                    foundFuel = true
                    continue
                }
                this.unlockAll()
                setLockingState(LockingState.MISSING_FUEL)
                return false
            }
        }
        if (!foundFuel) {
            this.unlockAll()
            setLockingState(LockingState.NO_FUEL_CHANNELS)
            return false
        }
        return true
    }

    private fun addReactorComponents() {
        if (reactor == null || getLevel() == null) {
            return
        }
        reactor!!.turnOff()
        val radius = this.diameter / 2
        val size = this.diameter - 2
        val reactorOrigin = getPos().mutable()
        reactorOrigin.move(getFrontFacing().getOpposite(), radius)
        for (x in 0..<size) {
            for (y in 0..<size) {
                val i = x - (radius - 1)
                val j = y - (radius - 1)
                if (i.toDouble().pow(2.0) + j.toDouble().pow(2.0) > radius.toDouble().pow(2.0) + radius) {
                    continue
                }
                val currentPos = reactorOrigin.mutable().move(this.reactorRight, i)
                    .move(getFrontFacing().getOpposite(), j)
                    .move(this.reactorUp, heightTop)
                    .immutable()
                val machine = getMachine(getLevel(), currentPos)
                if (machine is ICoolantHandler) {
                    val lockedFluid = machine.getLockedObject()
                    val stats = CoolantRegistry.getCoolant(lockedFluid)
                    machine.setCoolant(stats)
                    if (machine.getOutputHandler() != null) {
                        machine.getOutputHandler()!!.setCoolant(stats)
                    }
                    val component = CoolantChannel(100050.0, 0.0, stats, 1000.0)
                    component.setHandlers(machine, machine.getOutputHandler())
                    reactor!!.setComponent(x, y, component)
                } else if (machine is IFuelRodHandler) {
                    val lockedFuel = machine.getInputStackHandler().getStackInSlot(0)
                    val stats = FissionFuelRegistry.getFissionFuel(lockedFuel)
                    val component: FuelRod?
                    machine.setFuel(stats)
                    if (machine.getDepletionPoint() == 0.0 || machine.getPartialFuel() == null) {
                        machine.setPartialFuel(stats)
                        component = FuelRod(stats!!.getMaxTemperature().toDouble(), 1.0, stats, 650.0)
                        machine.getInputStackHandler().extractItem(0, 1, false)
                        machine.markUndepleted()
                    } else {
                        val partialProp = machine.getPartialFuel()
                        component = FuelRod(partialProp.getMaxTemperature().toDouble(), 1.0, partialProp, 650.0)
                    }
                    machine.setInternalFuelRod(component)
                    reactor!!.setComponent(x, y, component)
                } else if (machine is MetaTileEntityControlRodPort) {
                    val component = ControlRod(100000.0, machine.hasModeratorTip(), 1.0, 800.0)
                    reactor!!.setComponent(x, y, component)
                } else if (machine is MetaTileEntityModeratorPort) {
                    val moderator = machine.getModerator()
                    val component = Moderator(0.5, 800.0, moderator)
                    reactor!!.setComponent(x, y, component)
                }
            }
        }
    }

    private val coolantHandlers: MutableList<ICoolantHandler>
        get() = getParts().stream()
            .filter { part: IMultiPart? -> part is ICoolantHandler }
            .map<ICoolantHandler?> { part: IMultiPart? -> part as ICoolantHandler }
            .toList()

    fun rebuildReactor() {
        val size = max(3, (diameter - 2) or 1)
        val depth = max(1, this.height - 2)
        if (reactor != null && reactorSize == size && reactorDepth == depth) {
            return
        }
        reactorSize = size
        reactorDepth = depth
        val old = reactor
        reactor = FissionReactor(size, depth, controlRodInsertion)
        if (old != null) {
            reactor!!.setOn(old.isOn())
            reactor!!.kEff = old.kEff
            reactor!!.power = old.power
            reactor!!.temperature = old.temperature
            reactor!!.pressure = old.pressure
            reactor!!.fuelDepletion = old.fuelDepletion
            reactor!!.accumulatedHydrogen = old.accumulatedHydrogen
        }
        reactor!!.prepareThermalProperties()
        reactor!!.computeGeometry()
    }

    fun tickReactor() {
        if (reactor == null || !locked || !isFormed() || meltdown || pressureExplosion) return
        reactor!!.setOn(true)
        reactor!!.controlRodInsertion = controlRodInsertion
        reactor!!.tick()
        if (getOffsetTimer() % 20 == 0L) {
            handleFuel()
        }
        checkFailureState()
    }

    private fun handleFuel() {
        var canWork = true
        for (fuelImport in this.fuelRodHandlers) {
            if (fuelImport.isDepleted(reactor!!.fuelDepletion)) {
                val output = fuelImport.getOutputStackHandler(this.height - 1)
                if (output == null || !output.insertItem(0, fuelImport.getDepletedFuel(), true).isEmpty()) {
                    canWork = false
                    setLockingState(LockingState.FUEL_CLOGGED)
                    setLocked(false)
                    break
                }
                output.insertItem(0, fuelImport.getDepletedFuel(), false)
                fuelImport.markUndepleted()
                val input = fuelImport.getInputStackHandler()
                if (input.extractItem(0, 1, true).isEmpty()) {
                    canWork = false
                    fuelImport.setPartialFuel(null)
                    setLockingState(LockingState.MISSING_FUEL)
                    setLocked(false)
                    break
                }
                input.extractItem(0, 1, false)
            }
        }
        if (!canWork) {
            reactor!!.setOn(false)
        }
    }

    val height: Int
        get() = heightTop + heightBottom + 1

    private val fuelRodHandlers: MutableList<IFuelRodHandler>
        get() = getParts().stream()
            .filter { part: IMultiPart? -> part is IFuelRodHandler }
            .map<IFuelRodHandler?> { part: IMultiPart? -> part as IFuelRodHandler }
            .toList()

    private fun checkFailureState() {
        if (reactor == null) return
        if (SCConfigHolder.NUCLEAR.enableMeltdown.get() && reactor!!.temperature >= reactor!!.maxTemperature) {
            meltdown = true
            locked = false
            reactor!!.setOn(false)
            markDirty()
        }
        if (reactor!!.pressure >= reactor!!.maxPressure) {
            pressureExplosion = true
            locked = false
            reactor!!.setOn(false)
            markDirty()
        }
    }

    fun addDisplayText(text: MutableList<Component?>) {
        text.add(Component.translatable("supercritical.gui.fission.lock." + lockingState.name.lowercase(Locale.getDefault())))
        text.add(Component.translatable("supercritical.multiblock.fission_reactor.diameter", diameter))
        text.add(
            Component.translatable(
                "supercritical.multiblock.fission_reactor.height",
                heightTop + heightBottom + 1
            )
        )
        text.add(
            Component.translatable(
                "supercritical.gui.fission.control_rod_insertion",
                Math.round(controlRodInsertion * 100.0)
            )
        )
        if (meltdown) {
            text.add(Component.translatable("supercritical.multiblock.fission_reactor.meltdown"))
        }
        if (pressureExplosion) {
            text.add(Component.translatable("supercritical.multiblock.fission_reactor.pressure_explosion"))
        }
        if (reactor != null) {
            text.add(Component.translatable("supercritical.gui.fission.temperature", reactor!!.temperature))
            text.add(Component.translatable("supercritical.gui.fission.pressure", reactor!!.pressure))
            text.add(Component.translatable("supercritical.gui.fission.power", reactor!!.power, reactor!!.maxPower))
            text.add(Component.translatable("supercritical.gui.fission.k_eff", reactor!!.kEff))
        }
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        tag.putBoolean("Locked", locked)
        tag.putString("LockingState", lockingState.name)
        tag.putInt("Diameter", diameter)
        tag.putInt("HeightTop", heightTop)
        tag.putInt("HeightBottom", heightBottom)
        tag.putDouble("ControlRodInsertion", controlRodInsertion)
        tag.putBoolean("Meltdown", meltdown)
        tag.putBoolean("PressureExplosion", pressureExplosion)
        if (reactor != null) {
            tag.putBoolean("ReactorOn", reactor!!.isOn())
            tag.putDouble("KEff", reactor!!.kEff)
            tag.putDouble("Power", reactor!!.power)
            tag.putDouble("Temperature", reactor!!.temperature)
            tag.putDouble("Pressure", reactor!!.pressure)
            tag.putDouble("FuelDepletion", reactor!!.fuelDepletion)
            tag.putDouble("AccumulatedHydrogen", reactor!!.accumulatedHydrogen)
        }
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        locked = tag.getBoolean("Locked")
        if (tag.contains("LockingState")) {
            try {
                lockingState = LockingState.valueOf(tag.getString("LockingState"))
            } catch (ignored: IllegalArgumentException) {
                lockingState = if (locked) LockingState.LOCKED else LockingState.UNLOCKED
            }
        } else {
            lockingState = if (locked) LockingState.LOCKED else LockingState.UNLOCKED
        }
        diameter = if (tag.contains("Diameter")) tag.getInt("Diameter") else 5
        heightTop = if (tag.contains("HeightTop")) tag.getInt("HeightTop") else 1
        heightBottom = if (tag.contains("HeightBottom")) tag.getInt("HeightBottom") else 1
        controlRodInsertion = if (tag.contains("ControlRodInsertion")) tag.getDouble("ControlRodInsertion") else 1.0
        meltdown = tag.getBoolean("Meltdown")
        pressureExplosion = tag.getBoolean("PressureExplosion")
        rebuildReactor()
        if (reactor != null) {
            reactor!!.setOn(tag.getBoolean("ReactorOn"))
            reactor!!.kEff = tag.getDouble("KEff")
            reactor!!.power = tag.getDouble("Power")
            reactor!!.temperature =
                if (tag.contains("Temperature")) tag.getDouble("Temperature") else FissionReactor.Companion.ROOM_TEMPERATURE
            reactor!!.pressure =
                if (tag.contains("Pressure")) tag.getDouble("Pressure") else FissionReactor.Companion.STANDARD_PRESSURE
            reactor!!.fuelDepletion = if (tag.contains("FuelDepletion")) tag.getDouble("FuelDepletion") else -1.0
            reactor!!.accumulatedHydrogen = tag.getDouble("AccumulatedHydrogen")
        }
    }

    fun canToggle(): Boolean {
        return isFormed() && !meltdown && !pressureExplosion && (reactor != null || !locked)
                && (!locked || SCConfigHolder.NUCLEAR.enableMeltdown.get() || reactor!!.temperature < reactor!!.maxTemperature)
    }

    fun setLocked(locked: Boolean) {
        if (!canToggle() && locked) return
        this.locked = locked
        if (locked) {
            setLockingState(LockingState.SHOULD_LOCK)
            if (!lockAndPrepareReactor()) {
                if (reactor != null) {
                    reactor!!.setOn(false)
                }
                return
            }
        } else {
            unlockAll()
            if (lockingState == LockingState.LOCKED || lockingState == LockingState.SHOULD_LOCK) {
                setLockingState(LockingState.UNLOCKED)
            }
        }
        if (reactor != null) {
            reactor!!.setOn(locked)
        }
    }

    fun isLocked(): Boolean {
        return locked
    }

    fun resetFailureState() {
        meltdown = false
        pressureExplosion = false
        locked = false
        setLockingState(LockingState.UNLOCKED)
        if (reactor != null) {
            reactor!!.setOn(false)
            reactor!!.temperature = FissionReactor.Companion.ROOM_TEMPERATURE
            reactor!!.pressure = FissionReactor.Companion.STANDARD_PRESSURE
            reactor!!.power = 0.0
        }
        markDirty()
    }

    fun hasMeltdown(): Boolean {
        return meltdown
    }

    fun hasPressureExplosion(): Boolean {
        return pressureExplosion
    }

    fun hasReactor(): Boolean {
        return reactor != null
    }

    fun getDiameter(): Int {
        return diameter
    }

    fun setDiameter(diameter: Int) {
        this.diameter = max(5, min(15, diameter or 1))
        rebuildReactor()
    }

    fun getHeightTop(): Int {
        return heightTop
    }

    fun setHeightTop(heightTop: Int) {
        this.heightTop = max(1, min(7, heightTop))
        rebuildReactor()
    }

    fun getHeightBottom(): Int {
        return heightBottom
    }

    fun setHeightBottom(heightBottom: Int) {
        this.heightBottom = max(1, min(7, heightBottom))
        rebuildReactor()
    }

    fun getControlRodInsertion(): Double {
        return controlRodInsertion
    }

    fun setControlRodInsertion(controlRodInsertion: Double) {
        this.controlRodInsertion = max(0.0, min(1.0, controlRodInsertion))
        if (reactor != null) {
            reactor!!.updateControlRodInsertion(this.controlRodInsertion)
        }
    }

    override fun getCoverCapacity(): Long {
        return if (reactor == null) 0L else (reactor!!.maxPower * 1e6).toLong()
    }

    override fun getCoverStored(): Long {
        return if (reactor == null) 0L else (reactor!!.power * 1e6).toLong()
    }

    private val heatFillPercentage: Double
        get() = if (reactor == null || reactor!!.maxTemperature <= 0.0) 0.0 else min(
            1.0,
            reactor!!.temperature / reactor!!.maxTemperature
        )

    private val pressureFillPercentage: Double
        get() = if (reactor == null || reactor!!.maxPressure <= 0.0) 0.0 else min(
            1.0,
            reactor!!.pressure / reactor!!.maxPressure
        )

    private val powerFillPercentage: Double
        get() = if (reactor == null || reactor!!.maxPower <= 0.0) 0.0 else min(
            1.0,
            reactor!!.power / reactor!!.maxPower
        )

    private fun setLockingState(lockingState: LockingState) {
        this.lockingState = lockingState
        markDirty()
    }

    enum class LockingState {
        LOCKED,
        UNLOCKED,
        SHOULD_LOCK,
        MISSING_FUEL,
        MISSING_COOLANT,
        FUEL_CLOGGED,
        NO_FUEL_CHANNELS,
        INVALID_COMPONENT
    }

    override fun createUI(entityPlayer: Player): ModularUI {
        val screen = DraggableScrollableWidgetGroup(7, 4, 226, 109).setBackground(GuiTextures.DISPLAY)
        screen.addWidget(LabelWidget(4, 5, self().getDefinition().getDescriptionId()))
        screen.addWidget(
            ComponentPanelWidget(4, 17, Consumer { text: MutableList<Component?>? -> this.addDisplayText(text!!) })
                .textSupplier(if (self().getLevel()!!.isClientSide) null else Consumer { text: MutableList<Component?>? ->
                    this.addDisplayText(
                        text!!
                    )
                })
                .setMaxWidthLimit(216)
        )

        return ModularUI(240, 208, this, entityPlayer)
            .background(GuiTextures.BACKGROUND)
            .widget(screen)
            .widget(
                ProgressWidget(
                    DoubleSupplier { this.heatFillPercentage }, 4, 115, 76, 7,
                    GuiTextures.PROGRESS_BAR_BOILER_HEAT
                )
                    .setHoverTooltips(
                        Component.translatable(
                            "supercritical.gui.fission.temperature",
                            if (reactor == null) 0.0 else reactor!!.temperature
                        ).getString()
                    )
            )
            .widget(
                ProgressWidget(
                    DoubleSupplier { this.pressureFillPercentage }, 82, 115, 76, 7,
                    GuiTextures.PROGRESS_BAR_COMPRESS
                )
                    .setHoverTooltips(
                        Component.translatable(
                            "supercritical.gui.fission.pressure",
                            if (reactor == null) 0.0 else reactor!!.pressure
                        ).getString()
                    )
            )
            .widget(
                ProgressWidget(
                    DoubleSupplier { this.powerFillPercentage }, 160, 115, 76, 7,
                    GuiTextures.PROGRESS_BAR_ARROW
                )
                    .setHoverTooltips(
                        Component.translatable(
                            "supercritical.gui.fission.power",
                            if (reactor == null) 0.0 else reactor!!.power,
                            if (reactor == null) 0.0 else reactor!!.maxPower
                        ).getString()
                    )
            )
            .widget(LabelWidget(10, 132, Supplier {
                Component.translatable(
                    "supercritical.gui.fission.control_rod_insertion",
                    String.format("%.2f%%", controlRodInsertion * 100.0)
                ).getString()
            }))
            .widget(
                ButtonWidget(
                    10, 146, 20, 18,
                    TextTexture("-"), Consumer { cd: ClickData? ->
                        if (!cd!!.isRemote) {
                            setControlRodInsertion(controlRodInsertion - 0.01)
                        }
                    }).setHoverTooltips("gui.widget.incrementButton.default_tooltip")
            )
            .widget(
                ButtonWidget(
                    34, 146, 20, 18,
                    TextTexture("+"), Consumer { cd: ClickData? ->
                        if (!cd!!.isRemote) {
                            setControlRodInsertion(controlRodInsertion + 0.01)
                        }
                    }).setHoverTooltips("gui.widget.incrementButton.default_tooltip")
            )
            .widget(
                ToggleButtonWidget(
                    215,
                    125,
                    18,
                    18,
                    GuiTextures.BUTTON_WORKING_ENABLE,
                    BooleanSupplier { reactor != null && reactor!!.controlRodRegulationOn },
                    BooleanConsumer { enabled: Boolean ->
                        if (reactor != null) {
                            reactor!!.controlRodRegulationOn = enabled
                        }
                    })
                    .setShouldUseBaseBackground()
                    .setTooltipText("supercritical.gui.fission.helper")
            )
            .widget(
                ToggleButtonWidget(
                    215,
                    183,
                    18,
                    18,
                    GuiTextures.BUTTON_LOCK,
                    BooleanSupplier { this.isLocked() },
                    BooleanConsumer { locked: Boolean -> this.setLocked(locked) })
                    .setShouldUseBaseBackground()
                    .setTooltipText("supercritical.gui.fission.lock")
            )
            .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 125, true))
    }

    companion object {
        fun buildPattern(
            definition: MultiblockMachineDefinition,
            diameter: Int,
            heightBottom: Int,
            heightTop: Int
        ): BlockPattern? {
            val radius = if (diameter % 2 == 0) floor((diameter / 2f).toDouble()).toInt() else
                Math.round((diameter - 1) / 2f)

            val interiorBuilder = StringBuilder()
            val interiorSlice = arrayOfNulls<String>(diameter)

            for (i in 0..<diameter) {
                for (j in 0..<diameter) {
                    if ((i - floor(diameter / 2.0)).pow(2.0) + (j - floor(diameter / 2.0)).pow(2.0) < (radius + 0.5f).toDouble()
                            .pow(2.0)
                    ) {
                        interiorBuilder.append('A')
                    } else {
                        interiorBuilder.append(' ')
                    }
                }
                interiorSlice[i] = interiorBuilder.toString()
                interiorBuilder.setLength(0)
            }

            interiorSlice[0] = interiorSlice[0]!!.replace('A', 'B')
            interiorSlice[diameter - 1] = interiorSlice[0]
            for (i in 1..<diameter - 1) {
                for (j in 0..<diameter) {
                    if (interiorSlice[i]!!.get(j) != 'A') {
                        continue
                    }
                    val outerI = i + sign((i - (diameter / 2)).toFloat()).toInt()
                    if ((outerI - floor(diameter / 2.0)).pow(2.0) + (j - floor(diameter / 2.0)).pow(2.0) > (radius + 0.5f).toDouble()
                            .pow(2.0)
                    ) {
                        interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'B')
                    }
                    val outerJ = j + sign((j - (diameter / 2)).toFloat()).toInt()
                    if ((i - floor(diameter / 2.0)).pow(2.0) + (outerJ - floor(diameter / 2.0)).pow(2.0) > (radius + 0.5f).toDouble()
                            .pow(2.0)
                    ) {
                        interiorSlice[i] = SCUtility.replace(interiorSlice[i], j, 'B')
                    }
                }
            }

            val controllerSlice = interiorSlice.clone()
            val topSlice = interiorSlice.clone()
            val bottomSlice = interiorSlice.clone()
            controllerSlice[0] = controllerSlice[0]!!.substring(0, floor(diameter / 2.0).toInt()) + 'S' +
                    controllerSlice[0]!!.substring(floor(diameter / 2.0).toInt() + 1)
            for (i in 0..<diameter) {
                topSlice[i] = topSlice[i]!!.replace('A', 'I')
                bottomSlice[i] = bottomSlice[i]!!.replace('A', 'O')
            }

            return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
                .aisle(*bottomSlice)
                .aisle(*interiorSlice).setRepeatable(heightBottom - 1)
                .aisle(*controllerSlice)
                .aisle(*interiorSlice).setRepeatable(heightTop - 1)
                .aisle(*topSlice)
                .where('S', Predicates.controller(Predicates.blocks(definition.getBlock())))
                .where(
                    'A', Predicates.blocks(
                        SCBlocks.FUEL_CHANNEL.get(), SCBlocks.CONTROL_ROD_CHANNEL.get(),
                        SCBlocks.COOLANT_CHANNEL.get()
                    )
                        .or(Predicates.air())
                )
                .where(
                    'I', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get())
                        .or(
                            Predicates.abilities(
                                SCMultiblockAbility.IMPORT_COOLANT, SCMultiblockAbility.IMPORT_FUEL_ROD,
                                SCMultiblockAbility.CONTROL_ROD_PORT, SCMultiblockAbility.MODERATOR_PORT
                            )
                        )
                )
                .where(
                    'O', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get())
                        .or(
                            Predicates.abilities(
                                SCMultiblockAbility.EXPORT_COOLANT,
                                SCMultiblockAbility.EXPORT_FUEL_ROD
                            )
                        )
                )
                .where(
                    'B', Predicates.blocks(SCBlocks.REACTOR_VESSEL.get())
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1).setMaxGlobalLimited(1))
                )
                .where(' ', Predicates.any())
                .build()
        }
    }
}
