package io.github.symmetricdevs.supercritical.common.machine.multiblock.fission

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.gregtechceu.gtceu.api.gui.widget.ExtendedProgressWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.pattern.*
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture
import com.lowdragmc.lowdraglib.gui.texture.TextTexture
import com.lowdragmc.lowdraglib.gui.widget.*
import io.github.symmetricdevs.supercritical.api.gui.widget.ScritSliderWidget
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.utils.BlockInfo
import net.minecraft.core.BlockPos
import net.minecraft.core.BlockPos.MutableBlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level
import net.minecraftforge.event.ForgeEventFactory
import io.github.symmetricdevs.supercritical.Supercritical
import io.github.symmetricdevs.supercritical.api.capability.ICoolantHandler
import io.github.symmetricdevs.supercritical.api.capability.IFuelRodHandler
import io.github.symmetricdevs.supercritical.api.cover.ICustomEnergyCover
import io.github.symmetricdevs.supercritical.api.gui.ScritGuiTextures
import io.github.symmetricdevs.supercritical.api.machine.multiblock.IFissionReactorHatch
import io.github.symmetricdevs.supercritical.api.machine.multiblock.ScritMultiblockAbility
import io.github.symmetricdevs.supercritical.api.nuclear.fission.CoolantRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionFuelRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ModeratorRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.ControlRod
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.CoolantChannel
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.FuelRod
import io.github.symmetricdevs.supercritical.api.nuclear.fission.components.Moderator
import io.github.symmetricdevs.supercritical.common.data.ScritBlocks
import io.github.symmetricdevs.supercritical.common.data.ScritMaterials
import io.github.symmetricdevs.supercritical.common.machine.multiblock.part.ControlRodPort
import io.github.symmetricdevs.supercritical.common.machine.multiblock.part.ModeratorPort
import io.github.symmetricdevs.supercritical.config.ScritConfig
import io.github.symmetricdevs.supercritical.util.replace
import java.util.*
import java.util.function.Consumer
import kotlin.math.*

class FissionReactor(holder: IMachineBlockEntity) : MultiblockControllerMachine(holder), IFancyUIMachine,
    ICustomEnergyCover {
    override fun isRemote(): Boolean = super<MultiblockControllerMachine>.isRemote()

    var reactor: FissionReactor? = null
        private set

    @Persisted(key = "locked")
    @DescSynced
    private var lockedState = false

    var locked: Boolean
        get() = lockedState
        set(value) {
            applyLockedState(value)
        }

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

    // R1: mirrors of the reactor's runtime stats, replicated to the client every second by
    // syncReactorStats(). The GUI bars/text read these (not the non-synced `reactor` field),
    // so the display is live on the client. controlRodInsertion (above) is the 9th synced value.
    @DescSynced
    private var temperature = 0.0

    @DescSynced
    private var maxTemperature = 0.0

    @DescSynced
    private var pressure = 0.0

    @DescSynced
    private var maxPressure = 0.0

    @DescSynced
    private var power = 0.0

    @DescSynced
    private var maxPower = 0.0

    @DescSynced
    private var kEff = 0.0

    @DescSynced
    private var totalDepletion = 0.0

    override fun onLoad() {
        super.onLoad()
        val sub = tickSubscription
        if (sub == null || !sub.isStillSubscribed) {
            tickSubscription = subscribeServerTick { tickReactor() }
        }
    }

    override fun onUnload() {
        super.onUnload()
        val sub = tickSubscription
        if (sub != null) {
            sub.unsubscribe()
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
        // Legacy reset the sim's thermal/power state on structure invalidation, so an overheating
        // reactor defuses when broken (otherwise it re-melts immediately on reform). Mirrors
        // resetFailureState()'s thermal reset below.
        val r = reactor
        if (r != null) {
            r.isOn = false
            r.temperature = io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor.ROOM_TEMPERATURE
            r.pressure = io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor.STANDARD_PRESSURE
            r.power = 0.0
        }
    }

    private val reactorUp: Direction
        get() = RelativeDirection.UP.getRelative(frontFacing, upwardsFacing, isFlipped())

    private val reactorRight: Direction
        get() = RelativeDirection.RIGHT.getRelative(frontFacing, upwardsFacing, isFlipped())

    protected fun findHeight(top: Boolean): Int {
        var i = 1
        val pos = pos.mutable()
        val up = this.reactorUp
        val dir = if (top) up else up.opposite
        val level = level ?: return 0
        while (i <= 15) {
            if (isHeightEdge(level, pos, dir, i)) break
            i++
        }
        return i - 1
    }

    protected fun findDiameter(): Int {
        var i = 1
        val pos = pos.mutable()
        val level = level ?: return 0
        while (i <= 15) {
            pos.move(frontFacing.opposite)
            val state = level.getBlockState(pos)
            if (state.block === ScritBlocks.REACTOR_VESSEL.get()) {
                break
            }
            val machine = getMachine(level, pos)
            if (machine is IFissionReactorHatch) {
                break
            }
            if (PartAbility.MAINTENANCE.isApplicable(state.block)) {
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
        val block = state.block
        if (block === ScritBlocks.REACTOR_VESSEL.get()) {
            edge = true
        } else if (PartAbility.MAINTENANCE.isApplicable(block)) {
            edge = true
        } else {
            val machine = getMachine(level, pos)
            edge = machine is IFissionReactorHatch
        }
        pos.move(direction.opposite, steps)
        return edge
    }

    private fun moderatorPredicate(): TraceabilityPredicate {
        return Predicates.custom(
            { state: MultiblockState ->
                ModeratorRegistry.getModerator(
                    state.blockState.block
                ) != null
            },
            { arrayOfNulls<BlockInfo>(0) })
    }

    private fun buildDynamicPattern(): BlockPattern? {
        val radius = if (this.diameter % 2 == 0) floor((this.diameter / 2f).toDouble()).toInt() else
            Math.round((this.diameter - 1) / 2f)

        val interiorBuilder = StringBuilder()
        val interiorSlice = Array(this.diameter) { "" }
        val controllerSlice: Array<String>
        val topSlice: Array<String>
        val bottomSlice: Array<String>

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

        interiorSlice[0] = interiorSlice[0].replace('A', 'B')
        interiorSlice[this.diameter - 1] = interiorSlice[0]
        for (i in 1..<this.diameter - 1) {
            for (j in 0..<this.diameter) {
                if (interiorSlice[i][j] != 'A') {
                    continue
                }
                val outerI = i + sign((i - (this.diameter / 2)).toFloat()).toInt()
                if ((outerI - floor(this.diameter / 2.0)).pow(2.0) + (j - floor(this.diameter / 2.0)).pow(2.0) > (radius + 0.5f).toDouble()
                        .pow(2.0)
                ) {
                    interiorSlice[i] = interiorSlice[i].replace(j, 'B')
                }
                val outerJ = j + sign((j - (this.diameter / 2)).toFloat()).toInt()
                if ((i - floor(this.diameter / 2.0)).pow(2.0) + (outerJ - floor(this.diameter / 2.0)).pow(2.0) > (radius + 0.5f).toDouble()
                        .pow(2.0)
                ) {
                    interiorSlice[i] = interiorSlice[i].replace(j, 'B')
                }
            }
        }

        controllerSlice = interiorSlice.clone()
        topSlice = interiorSlice.clone()
        bottomSlice = interiorSlice.clone()
        controllerSlice[0] = controllerSlice[0].substring(0, floor(this.diameter / 2.0).toInt()) + 'S' +
                controllerSlice[0].substring(floor(this.diameter / 2.0).toInt() + 1)
        for (i in 0..<this.diameter) {
            topSlice[i] = topSlice[i].replace('A', 'I')
            bottomSlice[i] = bottomSlice[i].replace('A', 'O')
        }

        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.BACK, RelativeDirection.UP)
            .aisle(*bottomSlice)
            .aisle(*interiorSlice).setRepeatable(heightBottom - 1)
            .aisle(*controllerSlice)
            .aisle(*interiorSlice).setRepeatable(heightTop - 1)
            .aisle(*topSlice)
            .where('S', Predicates.controller(Predicates.blocks(definition.block)))
            .where(
                'A', Predicates.blocks(
                    ScritBlocks.FUEL_CHANNEL.get(), ScritBlocks.CONTROL_ROD_CHANNEL.get(),
                    ScritBlocks.COOLANT_CHANNEL.get()
                )
                    .or(Predicates.air())
                    .or(moderatorPredicate())
                    .or(Predicates.abilities(ScritMultiblockAbility.MODERATOR_PORT))
            )
            .where('I', Predicates.blocks(ScritBlocks.REACTOR_VESSEL.get()).or(this.importPredicate))
            .where(
                'O', Predicates.blocks(ScritBlocks.REACTOR_VESSEL.get())
                    .or(Predicates.abilities(ScritMultiblockAbility.EXPORT_COOLANT, ScritMultiblockAbility.EXPORT_FUEL_ROD))
            )
            .where(
                'B', Predicates.blocks(ScritBlocks.REACTOR_VESSEL.get())
                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1).setMaxGlobalLimited(1))
            )
            .where(' ', Predicates.any())
            .build()
    }

    private val importPredicate: TraceabilityPredicate
        get() {
            val allowedAbilities = arrayOf(
                ScritMultiblockAbility.IMPORT_COOLANT,
                ScritMultiblockAbility.IMPORT_FUEL_ROD,
                ScritMultiblockAbility.CONTROL_ROD_PORT,
                ScritMultiblockAbility.MODERATOR_PORT
            )
            return Predicates.custom({ state: MultiblockState ->
                val machine = getMachine(state.getWorld(), state.pos)
                if (machine !is IFissionReactorHatch) {
                    state.setError(
                        PatternStringError(
                            "supercritical.multiblock.pattern.error.hatch_invalid"
                        )
                    )
                    false
                } else {
                    val block = state.blockState.block
                    var allowed = false
                    for (ability in allowedAbilities) {
                        if (ability.isApplicable(block)) {
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
                        false
                    } else if (!machine.checkValidity(this.height - 1)) {
                        state.setError(
                            PatternStringError(
                                "supercritical.multiblock.pattern.error.hatch_invalid"
                            )
                        )
                        false
                    } else {
                        true
                    }
                }
            }, { arrayOfNulls<BlockInfo>(0) })
        }

    private fun lockAll() {
        for (handler in this.coolantHandlers) {
            handler.locked = true
        }
        for (handler in this.fuelRodHandlers) {
            handler.locked = true
        }
    }

    private fun unlockAll() {
        val r = reactor
        if (r != null) {
            val depletion = r.fuelDepletion
            for (handler in this.fuelRodHandlers) {
                handler.resetDepletion(depletion)
                handler.locked = false
            }
        } else {
            for (handler in this.fuelRodHandlers) {
                handler.locked = false
            }
        }
        for (handler in this.coolantHandlers) {
            handler.locked = false
        }
        if (r != null) {
            r.isOn = false
            r.resetFuelDepletion()
        }
    }

    private fun lockAndPrepareReactor(): Boolean {
        if (!verifyCorrectness()) {
            this.locked = false
            return false
        }
        this.lockAll()
        this.addReactorComponents()
        val r = reactor ?: return false
        r.prepareThermalProperties()
        r.computeGeometry()
        setLockingState(LockingState.LOCKED)
        return true
    }

    private fun verifyCorrectness(): Boolean {
        var foundFuel = false
        // [SC-DEBUG] temporary debug logging for the "missing coolant" startup bug; remove once
        // the issue is confirmed fixed.
        val debugCoolantHandlers = parts.filterIsInstance<ICoolantHandler>()
        Supercritical.LOGGER.info(
            "[SC-DEBUG] verifyCorrectness: {} total part(s); {} coolant handler(s) ({} import, {} export)",
            parts.size, debugCoolantHandlers.size,
            debugCoolantHandlers.count { it.outputHandler !== it },
            debugCoolantHandlers.count { it.outputHandler === it }
        )
        for (part in parts) {
            if (part is ICoolantHandler) {
                // Legacy 1.12.2 validated coolant by iterating the TOP (import) layer by position,
                // so export hatches were never checked here. The modern port iterates `parts`
                // (import + export), so explicitly skip export hatches: their outputHandler is
                // themselves, and they legitimately start empty (hot coolant is produced during
                // operation). Without this skip an empty export hatch always blocks startup.
                if (part.outputHandler === part) {
                    continue
                }
                // Legacy's LockableFluidTank kept `lockedFluid` in sync with the tank on every
                // fill(), so getLockedObject() returned the current fluid even before lockAll().
                // The modern LockableFluidTank only populates lockedObject inside the lock setter,
                // which runs AFTER this check (verifyCorrectness() precedes lockAll() in
                // lockAndPrepareReactor). Fall back to the fluid actually in the tank, otherwise a
                // filled-but-unlocked hatch (e.g. distilled water just pumped in) reads null and
                // wrongly reports MISSING_COOLANT.
                val tankFluid = part.fluidTank.getFluidInTank(0)
                val hatchFluid = part.lockedObject ?: (if (tankFluid.isEmpty) null else tankFluid.fluid)
                val coolantStat = hatchFluid?.let { CoolantRegistry.getCoolant(it) }
                Supercritical.LOGGER.info(
                    "[SC-DEBUG] coolant import hatch @ {}: fluid={} amount={} lockedObject={} resolved={} registeredCoolant={}",
                    part.hatchPos,
                    if (tankFluid.isEmpty) "empty" else tankFluid.fluid,
                    tankFluid.amount,
                    part.lockedObject,
                    hatchFluid,
                    coolantStat != null
                )
                if (hatchFluid != null) {
                    if (part.outputHandler == null && !part.checkValidity(this.height - 1)) {
                        Supercritical.LOGGER.info(
                            "[SC-DEBUG] -> INVALID_COMPONENT: hatch @ {} has no paired export",
                            part.hatchPos
                        )
                        setLockingState(LockingState.INVALID_COMPONENT)
                        return false
                    }
                    if (coolantStat != null) {
                        continue
                    }
                }
                Supercritical.LOGGER.info(
                    "[SC-DEBUG] -> MISSING_COOLANT: hatch @ {} resolved fluid={} registeredCoolant={}",
                    part.hatchPos, hatchFluid, coolantStat != null
                )
                this.unlockAll()
                setLockingState(LockingState.MISSING_COOLANT)
                return false
            } else if (part is IFuelRodHandler) {
                val inputHandler = part.inputStackHandler
                if (inputHandler == null) {
                    this.unlockAll()
                    setLockingState(LockingState.MISSING_FUEL)
                    return false
                }
                val lockedFuel = inputHandler.getStackInSlot(0)
                if (!lockedFuel.isEmpty) {
                    val stats = FissionFuelRegistry.getFissionFuel(lockedFuel)
                    if (stats != null) {
                        foundFuel = true
                        continue
                    }
                } else if (part.partialFuel != null) {
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
        val level = level ?: return
        val r = reactor ?: return
        r.turnOff()
        val radius = this.diameter / 2
        val size = this.diameter - 2
        val reactorOrigin = pos.mutable()
        reactorOrigin.move(frontFacing.opposite, radius)
        for (x in 0..<size) {
            for (y in 0..<size) {
                val i = x - (radius - 1)
                val j = y - (radius - 1)
                if (i.toDouble().pow(2.0) + j.toDouble().pow(2.0) > radius.toDouble().pow(2.0) + radius) {
                    continue
                }
                val currentPos = reactorOrigin.mutable().move(this.reactorRight, i)
                    .move(frontFacing.opposite, j)
                    .move(this.reactorUp, heightTop)
                    .immutable()
                val machine = getMachine(level, currentPos)
                if (machine is ICoolantHandler) {
                    val lockedFluid = machine.lockedObject
                    val stats = CoolantRegistry.getCoolant(lockedFluid) ?: continue
                    machine.coolant = stats
                    val outputHandler = machine.outputHandler
                    if (outputHandler != null) {
                        outputHandler.coolant = stats
                    }
                    val component = CoolantChannel(100050.0, 0.0, stats, 1000.0)
                    component.setHandlers(machine, outputHandler)
                    r.setComponent(x, y, component)
                } else if (machine is IFuelRodHandler) {
                    val partialFuel = machine.partialFuel
                    val inputHandler = machine.inputStackHandler
                    val lockedFuel = inputHandler?.getStackInSlot(0)
                    val inputFuel = lockedFuel
                        ?.takeUnless { it.isEmpty }
                        ?.let { FissionFuelRegistry.getFissionFuel(it) }
                    val fuel = partialFuel ?: inputFuel ?: continue
                    val component: FuelRod
                    machine.fuel = fuel
                    if (partialFuel == null) {
                        machine.setPartialFuel(fuel)
                        component = FuelRod(fuel.maxTemperature.toDouble(), 1.0, fuel, 650.0)
                        inputHandler!!.extractItem(0, 1, false)
                        machine.markUndepleted()
                    } else {
                        component = FuelRod(partialFuel.maxTemperature.toDouble(), 1.0, partialFuel, 650.0)
                    }
                    machine.setInternalFuelRod(component)
                    r.setComponent(x, y, component)
                } else if (machine is ControlRodPort) {
                    val component = ControlRod(100000.0, machine.hasModeratorTip(), 1.0, 800.0)
                    r.setComponent(x, y, component)
                } else if (machine is ModeratorPort) {
                    val moderator = machine.moderator ?: continue
                    val component = Moderator(0.5, 800.0, moderator)
                    r.setComponent(x, y, component)
                }
            }
        }
    }

    private val coolantHandlers: List<ICoolantHandler>
        get() = parts.filterIsInstance<ICoolantHandler>()

    fun rebuildReactor() {
        val size = max(3, (diameter - 2) or 1)
        val depth = max(1, this.height - 2)
        if (reactor != null && reactorSize == size && reactorDepth == depth) {
            return
        }
        reactorSize = size
        reactorDepth = depth
        val old = reactor
        val newReactor = FissionReactor(size, depth, controlRodInsertion)
        reactor = newReactor
        if (old != null) {
            newReactor.isOn = old.isOn
            newReactor.kEff = old.kEff
            newReactor.power = old.power
            newReactor.temperature = old.temperature
            newReactor.pressure = old.pressure
            newReactor.fuelDepletion = old.fuelDepletion
            newReactor.accumulatedHydrogen = old.accumulatedHydrogen
        }
        newReactor.prepareThermalProperties()
        newReactor.computeGeometry()
    }

    fun tickReactor() {
        val r = reactor ?: return
        if (!isFormed()) return
        // Legacy runs updateReactorState once per second (getOffsetTimer() % 20 == 0). The
        // FissionReactor formulas are calibrated for 1-second steps, so ticking every server
        // tick would compound neutron flux/depletion/temperature ~20x.
        if (offsetTimer % 20 != 0L) return

        if (locked && !meltdown && !pressureExplosion) {
            // Locked full-power path.
            r.isOn = true
            // controlRodInsertion is pushed to the reactor via setControlRodInsertion and the
            // constructor; regulateControlRods adjusts the reactor copy and syncReactorStats
            // mirrors it back, so we intentionally do NOT overwrite it here every tick.
            r.tick()
            handleFuel()
        } else {
            // Cooldown / relaxation path (unlocked, or after a meltdown/pressure explosion).
            // Legacy calls updateReactorState whenever the structure is formed+valid even when
            // unlocked, so an SCRAMed reactor ramps power/neutronFlux down (isOn=false branch)
            // and relaxes temperature/pressure. We run the individual update methods directly
            // (bypassing FissionReactor.tick()'s !isOn early-return) to get that cooldown, but
            // WITHOUT the fuel/lock side effects of the locked path.
            r.isOn = false
            r.updatePower()
            r.updateTemperature()
            r.updatePressure()
            r.updateNeutronPoisoning()
            r.regulateControlRods()
        }
        // Legacy runs the meltdown/explosion checks whenever the structure is valid (even
        // unlocked), because a just-SCRAMed reactor is still hot.
        checkFailureState()
        // Replicate the (post-update) reactor stats to the client once per second.
        syncReactorStats()
    }

    /**
     * Mirror of legacy syncReactorStats: copies the reactor's 9 display stats into @DescSynced
     * managed fields so LDLib replicates them to the client. The client GUI reads these fields.
     */
    private fun syncReactorStats() {
        val r = reactor ?: return
        temperature = r.temperature
        maxTemperature = r.maxTemperature
        pressure = r.pressure
        maxPressure = r.maxPressure
        power = r.power
        maxPower = r.maxPower
        kEff = r.kEff
        controlRodInsertion = r.controlRodInsertion
        totalDepletion = r.fuelDepletion
    }

    private fun handleFuel() {
        val r = reactor ?: return
        var canWork = true
        for (fuelImport in this.fuelRodHandlers) {
            if (fuelImport.isDepleted(r.fuelDepletion)) {
                val output = fuelImport.getOutputStackHandler(this.height - 1)
                if (output == null || !output.insertItem(0, fuelImport.depletedFuel, true).isEmpty) {
                    canWork = false
                    setLockingState(LockingState.FUEL_CLOGGED)
                    locked = false
                    break
                }
                output.insertItem(0, fuelImport.depletedFuel, false)
                fuelImport.markUndepleted()
                val input = fuelImport.inputStackHandler
                if (input == null || input.extractItem(0, 1, true).isEmpty) {
                    canWork = false
                    fuelImport.setPartialFuel(null)
                    setLockingState(LockingState.MISSING_FUEL)
                    locked = false
                    break
                }
                input.extractItem(0, 1, false)
            }
        }
        if (!canWork) {
            r.isOn = false
        }
    }

    val height: Int
        get() = heightTop + heightBottom + 1

    private val fuelRodHandlers: List<IFuelRodHandler>
        get() = parts.filterIsInstance<IFuelRodHandler>()

    private fun checkFailureState() {
        val r = reactor ?: return
        // Legacy gates both the meltdown and pressure-explosion checks behind enableMeltdown
        // (updateFormedValid returns early when !enableMeltdown), so disabling meltdown
        // suppresses both failure modes and their world effects.
        if (!ScritConfig.INSTANCE.nuclear.enableMeltdown) return

        val melts = !meltdown && r.temperature > r.maxTemperature
        val explodes = !pressureExplosion && r.pressure > r.maxPressure
        if (!melts && !explodes) return

        if (melts) {
            meltdown = true
            performMeltdownEffects()
        }
        if (explodes) {
            pressureExplosion = true
            performPrimaryExplosion()
            if (r.accumulatedHydrogen > 1.0) {
                performSecondaryExplosion(r.accumulatedHydrogen)
            }
        }
        // Effects fire once on the false->true transition; then latch the failed state.
        locked = false
        r.isOn = false
        markDirty()
    }

    /**
     * Faithful port of legacy performMeltdownEffects: places Corium fluid columns down through
     * the structure. For each block in the cylindrical footprint, decide whether it melts
     * (1/10 random chance, or always for the bottom layer and for fuel-rod hatch columns), then
     * for each melted position fill corium downward to the bottom of the structure.
     */
    private fun performMeltdownEffects() {
        val level = level ?: return
        unlockAll()
        val coriumFluid = ScritMaterials.Corium.fluid
        val coriumState = coriumFluid?.defaultFluidState()?.createLegacyBlock() ?: return
        val controllerPos = pos
        val radius = diameter / 2
        val bottomDy = -heightBottom
        val topDy = heightTop
        val front = frontFacing.opposite
        val right = reactorRight
        val up = reactorUp
        val random = level.random
        val meltsDown = LinkedHashSet<BlockPos>()
        for (i in -radius..radius) {
            for (j in -radius..radius) {
                // Same circular bound used by addReactorComponents.
                if (i * i + j * j > radius * radius + radius) continue
                for (dy in bottomDy..topDy) {
                    val pos = controllerPos.mutable()
                        .move(front, radius + j)
                        .move(right, i)
                        .move(up, dy)
                    // Never replace the controller MTE itself. Legacy could melt the controller
                    // position (it lived in the pattern cache); in modern doing so orphans this
                    // machine's tick subscription, so we skip it. The meltdown still guts the
                    // surrounding structure.
                    if (pos.x == controllerPos.x && pos.y == controllerPos.y && pos.z == controllerPos.z) continue
                    var chance = 10
                    if (dy == bottomDy) {
                        chance = 1
                    } else {
                        val machine = getMachine(level, pos)
                        if (machine is IFuelRodHandler) chance = 1
                    }
                    if (random.nextInt(chance) == 0) {
                        meltsDown.add(pos.immutable())
                    }
                }
            }
        }
        for (immutPos in meltsDown) {
            val pos = immutPos.mutable()
            while (pos.y >= controllerPos.y + bottomDy) {
                level.setBlock(pos, coriumState, 3)
                pos.move(Direction.DOWN)
            }
        }
    }

    /**
     * Faithful port of legacy performPrimaryExplosion: a 4f block-breaking explosion at the top
     * of the reactor center column. Called once when pressure first exceeds maxPressure.
     */
    private fun performPrimaryExplosion() {
        val level = level ?: return
        unlockAll()
        val center = pos.mutable().move(frontFacing.opposite, diameter / 2)
        detonate(level, center.x.toDouble(), (pos.y + heightTop).toDouble(), center.z.toDouble(), 4f, false)
    }

    /**
     * Faithful port of legacy performSecondaryExplosion: a fire+block-breaking explosion scaled
     * by log(accumulatedHydrogen), 3 blocks above the primary. Only fires when hydrogen > 1.
     */
    private fun performSecondaryExplosion(accumulatedHydrogen: Double) {
        val level = level ?: return
        val center = pos.mutable().move(frontFacing.opposite, diameter / 2)
        val strength = (5f + Math.log(accumulatedHydrogen).toFloat())
        detonate(
            level, center.x.toDouble(), (pos.y + heightTop + 3).toDouble(), center.z.toDouble(),
            strength, true
        )
    }

    private fun detonate(level: Level, x: Double, y: Double, z: Double, radius: Float, fire: Boolean) {
        // Construct Explosion directly so we can request fire (legacy newExplosion(flaming=true)
        // for the secondary). DESTROY mirrors legacy isSmoking=true block removal.
        val explosion = Explosion(level, null, x, y, z, radius, fire, Explosion.BlockInteraction.DESTROY)
        if (!ForgeEventFactory.onExplosionStart(level, explosion)) {
            explosion.explode()
            explosion.finalizeExplosion(true)
        }
    }

    fun addDisplayText(text: MutableList<Component?>) {
        // Matches the 1.12.2 panel (`addDisplayText`): the lock-state line and k_eff.
        // Temperature/pressure/power live on the gauge hover tooltips (legacy `addBarHoverText`,
        // here `setServerTooltipSupplier`), and the control-rod insertion is shown on the slider
        // overlay — so they are intentionally absent from this panel.
        text.add(Component.translatable("supercritical.gui.fission.lock." + lockingState.name.lowercase(Locale.getDefault())))
        if (meltdown) {
            text.add(Component.translatable("supercritical.multiblock.fission_reactor.meltdown"))
        }
        if (pressureExplosion) {
            text.add(Component.translatable("supercritical.multiblock.fission_reactor.pressure_explosion"))
        }
        text.add(Component.translatable("supercritical.gui.fission.k_eff", kEff))
    }

    override fun saveCustomPersistedData(tag: CompoundTag, forDrop: Boolean) {
        super.saveCustomPersistedData(tag, forDrop)
        tag.putBoolean("Locked", locked)
        tag.putString("LockingState", lockingState.name)
        tag.putInt("Diameter", diameter)
        tag.putInt("HeightTop", heightTop)
        tag.putInt("HeightBottom", heightBottom)
        tag.putDouble("ControlRodInsertion", controlRodInsertion)
        if (!forDrop) {
            // Meltdown-latch soft-lock guard: a melted/exploded controller must NOT carry these
            // latches into the dropped item. Otherwise wrenching + re-placing bricks the block
            // permanently (canToggle() stays false, the locked path stays blocked, and
            // resetFailureState() is unreachable). On reload, loadCustomPersistedData reads these
            // via CompoundTag#getBoolean, which defaults to false when the keys are absent — and
            // that manual read runs AFTER the @Persisted field restore, so it wins. The dropped
            // item therefore reloads to a clean meltdown=false / pressureExplosion=false state.
            tag.putBoolean("Meltdown", meltdown)
            tag.putBoolean("PressureExplosion", pressureExplosion)
        }
        val r = reactor
        if (r != null) {
            // R2: delegate to FissionReactor's faithful port of legacy serializeNBT, which also
            // persists the previously-dropped NeutronFlux/PrevTemperature/NeutronPoisonAmount/
            // DecayProductsAmount/ControlRodRegulationOn.
            tag.put("FissionReactor", r.serializeNBT())
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
        val r = reactor
        if (r != null && tag.contains("FissionReactor")) {
            r.deserializeNBT(tag.getCompound("FissionReactor"))
        }
    }

    fun canToggle(): Boolean {
        val r = reactor
        return isFormed() && !meltdown && !pressureExplosion && (r != null || !locked)
                && (!locked || ScritConfig.INSTANCE.nuclear.enableMeltdown || (r != null && r.temperature < r.maxTemperature))
    }

    private fun applyLockedState(locked: Boolean) {
        val r = reactor
        if (!canToggle() && locked) return
        this.lockedState = locked
        if (locked) {
            setLockingState(LockingState.SHOULD_LOCK)
            if (!lockAndPrepareReactor()) {
                if (r != null) {
                    r.isOn = false
                }
                return
            }
        } else {
            unlockAll()
            if (lockingState == LockingState.LOCKED || lockingState == LockingState.SHOULD_LOCK) {
                setLockingState(LockingState.UNLOCKED)
            }
        }
        if (r != null) {
            r.isOn = locked
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
        val r = reactor
        if (r != null) {
            r.isOn = false
            r.temperature = io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor.ROOM_TEMPERATURE
            r.pressure = io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor.STANDARD_PRESSURE
            r.power = 0.0
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
        reactor?.updateControlRodInsertion(this.controlRodInsertion)
    }

    // CC1: public getter API backing the CC:Tweaked peripheral provider (COMPUTER cluster).
    // Reads the live server-side reactor state; returns 0 when no reactor is formed.
    fun getMaxPower(): Double = reactor?.maxPower ?: 0.0

    fun getPower(): Double = reactor?.power ?: 0.0

    fun getMaxTemperature(): Double = reactor?.maxTemperature ?: 0.0

    fun getTemperature(): Double = reactor?.temperature ?: 0.0

    fun getMaxPressure(): Double = reactor?.maxPressure ?: 0.0

    fun getPressure(): Double = reactor?.pressure ?: 0.0

    fun areControlRodsRegulated(): Boolean = reactor?.controlRodRegulationOn ?: false

    fun toggleControlRodRegulation() {
        val r = reactor ?: return
        r.controlRodRegulationOn = !r.controlRodRegulationOn
    }

    // CC1: absolute-state setter backing the CC:Tweaked peripheral provider (COMPUTER cluster).
    // Legacy OC setSmiley(boolean) set absolute regulation state; the no-arg toggle above stays
    // for the GUI button.
    fun setControlRodRegulation(on: Boolean) {
        reactor?.controlRodRegulationOn = on
    }

    override val coverCapacity: Long
        get() = reactor?.let { (it.maxPower * 1e6).toLong() } ?: 0L

    override val coverStored: Long
        get() = reactor?.let { (it.power * 1e6).toLong() } ?: 0L

    // R1: read the @DescSynced mirrors so the client-side bars track server state.
    private val heatFillPercentage: Double
        get() = if (maxTemperature <= 0.0) 0.0 else min(1.0, temperature / maxTemperature)

    private val pressureFillPercentage: Double
        get() = if (maxPressure <= 0.0) 0.0 else min(1.0, pressure / maxPressure)

    // R4: restore legacy getFillPercentage(2) logarithmic scale, floored at 0 when the reactor is
    // far below max power (maxPower / power > e^9).
    private val powerFillPercentage: Double
        get() {
            if (maxPower <= 0.0 || power <= 0.0) return 0.0
            if (maxPower / power > Math.exp(9.0)) return 0.0
            return (Math.log(power / maxPower) + 9.0) / 9.0
        }

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

    override fun createUI(entityPlayer: Player): ModularUI =
        ModularUI(198, 208, this, entityPlayer).widget(FancyMachineUIWidget(this, 198, 208))

    override fun createUIWidget(): Widget {
        // Main page (GTCEu-standard 190×125): a DISPLAY status screen on top, the three live
        // gauges directly beneath it (NOT inside the screen), and the control-rod slider below
        // those — all above the player inventory (which FancyMachineUIWidget adds underneath).
        val isClient = self().level?.isClientSide == true
        val group = WidgetGroup(0, 0, 190, 125)
        // Status screen (scrollable) — top portion only, so the gauges sit below it, not in it.
        val screen = DraggableScrollableWidgetGroup(4, 4, 182, 92)
        screen.setBackground(GuiTextures.DISPLAY)
        screen.addWidget(LabelWidget(4, 5, self().definition.descriptionId))
        screen.addWidget(
            ComponentPanelWidget(4, 17, Consumer { text: MutableList<Component?> -> this.addDisplayText(text) })
                .textSupplier(if (isClient) null else Consumer { text: MutableList<Component?> ->
                    this.addDisplayText(text)
                })
                .setMaxWidthLimit(174)
        )
        group.addWidget(screen)
        // Three gauges across the page width, directly under the screen (reading the @DescSynced
        // stat mirrors so the bars/tooltips are live on the client).
        group.addWidget(
            ExtendedProgressWidget(
                { this.heatFillPercentage }, 4, 98, 60, 7,
                ScritGuiTextures.PROGRESS_BAR_FISSION_HEAT
            )
                .setServerTooltipSupplier { list ->
                    list.add(Component.translatable("supercritical.gui.fission.temperature", temperature))
                }
                .setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT)
        )
        group.addWidget(
            ExtendedProgressWidget(
                { this.pressureFillPercentage }, 65, 98, 60, 7,
                ScritGuiTextures.PROGRESS_BAR_FISSION_PRESSURE
            )
                .setServerTooltipSupplier { list ->
                    list.add(Component.translatable("supercritical.gui.fission.pressure", pressure))
                }
                .setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT)
        )
        group.addWidget(
            ExtendedProgressWidget(
                { this.powerFillPercentage }, 126, 98, 60, 7,
                ScritGuiTextures.PROGRESS_BAR_FISSION_ENERGY
            )
                .setServerTooltipSupplier { list ->
                    list.add(Component.translatable("supercritical.gui.fission.power", power, maxPower))
                }
                .setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT)
        )
        // Control-rod insertion slider — back on the main page, below the gauges.
        group.addWidget(
            ScritSliderWidget(2, 107, 186, 18).apply {
                setMinAmount(0f)
                setMaxAmount(1f)
                handleTexture = ScritGuiTextures.DARK_SLIDER_ICON
                handleHoverTexture = ScritGuiTextures.DARK_SLIDER_ICON
                setBackground(ScritGuiTextures.DARK_SLIDER_BACKGROUND)
                setSliderValueProvider { this@FissionReactor.controlRodInsertion.toFloat() }
                setResponder { this@FissionReactor.setControlRodInsertion(it.toDouble()) }
                setOverlay(
                    TextTexture {
                        Component.translatable(
                            "supercritical.gui.fission.control_rod_insertion",
                            "%.2f%%".format(sliderValue * 100f)
                        ).string.replace("%", "%%")
                    }
                )
            }
        )
        // No page background: only the DISPLAY panel above is the "screen"; the gauges + slider sit
        // on the FancyMachineUIWidget's standard background, below the screen and above the inventory.
        return group
    }

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        // Attached top→bottom: the smiley control-rod-regulation toggle, then the reactor
        // power/lock toggle. Do NOT call super — this replaces GTCEu's default IControllable
        // workingEnabled power button + cover configurators. The BUTTON_POWER toggle below IS the
        // reactor on/off (locked = running, base = stopped), bound to `locked` rather than
        // isWorkingEnabled because FissionReactor gates operation on its own lock state.
        configuratorPanel.attachConfigurators(
            IFancyConfiguratorButton.Toggle(
                // BUTTON_CONTROL_ROD_HELPER is an 18×36 two-state strip: top half = regulation off,
                // bottom half = regulation on.
                ScritGuiTextures.BUTTON_CONTROL_ROD_HELPER.getSubTexture(0f, 0f, 1f, 0.5f),
                ScritGuiTextures.BUTTON_CONTROL_ROD_HELPER.getSubTexture(0f, 0.5f, 1f, 0.5f),
                { this.areControlRodsRegulated() },
                { _, pressed -> this.setControlRodRegulation(pressed) }
            ).setTooltipsSupplier { pressed ->
                listOf(
                    Component.translatable(
                        if (pressed) "supercritical.gui.fission.helper.on"
                        else "supercritical.gui.fission.helper.off"
                    )
                )
            },
            IFancyConfiguratorButton.Toggle(
                // BUTTON_POWER split like GTCEu's IControllable default: pressed (bottom half) =
                // reactor running (locked), base (top half) = reactor stopped (unlocked).
                GuiTextures.BUTTON_POWER.getSubTexture(0f, 0f, 1f, 0.5f),
                GuiTextures.BUTTON_POWER.getSubTexture(0f, 0.5f, 1f, 0.5f),
                { this.isLocked() },
                { _, pressed -> this.locked = pressed }
            ).setTooltipsSupplier { pressed ->
                listOf(
                    Component.translatable(
                        if (pressed) "supercritical.gui.fission.lock.enabled"
                        else "supercritical.gui.fission.lock.disabled"
                    )
                )
            }
        )
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
            val interiorSlice = Array(diameter) { "" }

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

            interiorSlice[0] = interiorSlice[0].replace('A', 'B')
            interiorSlice[diameter - 1] = interiorSlice[0]
            for (i in 1..<diameter - 1) {
                for (j in 0..<diameter) {
                    if (interiorSlice[i][j] != 'A') {
                        continue
                    }
                    val outerI = i + sign((i - (diameter / 2)).toFloat()).toInt()
                    if ((outerI - floor(diameter / 2.0)).pow(2.0) + (j - floor(diameter / 2.0)).pow(2.0) > (radius + 0.5f).toDouble()
                            .pow(2.0)
                    ) {
                        interiorSlice[i] = interiorSlice[i].replace(j, 'B')
                    }
                    val outerJ = j + sign((j - (diameter / 2)).toFloat()).toInt()
                    if ((i - floor(diameter / 2.0)).pow(2.0) + (outerJ - floor(diameter / 2.0)).pow(2.0) > (radius + 0.5f).toDouble()
                            .pow(2.0)
                    ) {
                        interiorSlice[i] = interiorSlice[i].replace(j, 'B')
                    }
                }
            }

            val controllerSlice = interiorSlice.clone()
            val topSlice = interiorSlice.clone()
            val bottomSlice = interiorSlice.clone()
            controllerSlice[0] = controllerSlice[0].substring(0, floor(diameter / 2.0).toInt()) + 'S' +
                    controllerSlice[0].substring(floor(diameter / 2.0).toInt() + 1)
            for (i in 0..<diameter) {
                topSlice[i] = topSlice[i].replace('A', 'I')
                bottomSlice[i] = bottomSlice[i].replace('A', 'O')
            }

            return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.BACK, RelativeDirection.UP)
                .aisle(*bottomSlice)
                .aisle(*interiorSlice).setRepeatable(heightBottom - 1)
                .aisle(*controllerSlice)
                .aisle(*interiorSlice).setRepeatable(heightTop - 1)
                .aisle(*topSlice)
                .where('S', Predicates.controller(Predicates.blocks(definition.block)))
                .where(
                    'A', Predicates.blocks(
                        ScritBlocks.FUEL_CHANNEL.get(), ScritBlocks.CONTROL_ROD_CHANNEL.get(),
                        ScritBlocks.COOLANT_CHANNEL.get()
                    )
                        .or(Predicates.air())
                )
                .where(
                    'I', Predicates.blocks(ScritBlocks.REACTOR_VESSEL.get())
                        .or(
                            Predicates.abilities(
                                ScritMultiblockAbility.IMPORT_COOLANT, ScritMultiblockAbility.IMPORT_FUEL_ROD,
                                ScritMultiblockAbility.CONTROL_ROD_PORT, ScritMultiblockAbility.MODERATOR_PORT
                            )
                        )
                )
                .where(
                    'O', Predicates.blocks(ScritBlocks.REACTOR_VESSEL.get())
                        .or(
                            Predicates.abilities(
                                ScritMultiblockAbility.EXPORT_COOLANT,
                                ScritMultiblockAbility.EXPORT_FUEL_ROD
                            )
                        )
                )
                .where(
                    'B', Predicates.blocks(ScritBlocks.REACTOR_VESSEL.get())
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1).setMaxGlobalLimited(1))
                )
                .where(' ', Predicates.any())
                .build()
        }
    }
}
