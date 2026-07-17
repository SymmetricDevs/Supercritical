package io.github.symmetricdevs.supercritical.api.fission.reactor.pwr

import io.github.symmetricdevs.supercritical.api.fission.ecs.Entity
import io.github.symmetricdevs.supercritical.api.fission.ecs.World
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.ControlRodComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.ControlRodStateComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.CoolantChannelComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.FuelRodComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.LatticeGeometryComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.ModeratorComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.NeutronicsGlobalsComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.NeutronicsPropertiesComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.PositionComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.ReactorComponentTypes
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.ReactorFamilyComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.ReactorLimitsComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.ReactorStateComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.ThermalGlobalsComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.components.ThermalPropertiesComponent
import io.github.symmetricdevs.supercritical.api.fission.ecs.registration.ComponentTypeRegistry
import io.github.symmetricdevs.supercritical.api.fission.ecs.resources.ReactorCoreResource
import io.github.symmetricdevs.supercritical.api.fission.ecs.resources.RootEntityResource
import io.github.symmetricdevs.supercritical.api.fission.reactor.*
import io.github.symmetricdevs.supercritical.api.fission.reactor.control.LegacyControlRodBank
import io.github.symmetricdevs.supercritical.api.fission.reactor.families.LegacyPWRFamily
import io.github.symmetricdevs.supercritical.api.fission.reactor.systems.*
import io.github.symmetricdevs.supercritical.api.fission.reactor.systems.controlRodFactor
import io.github.symmetricdevs.supercritical.api.fission.stats.CoolantStats
import io.github.symmetricdevs.supercritical.api.fission.stats.FissionFuelStats
import io.github.symmetricdevs.supercritical.api.fission.stats.ModeratorStats
import net.minecraft.nbt.CompoundTag
import kotlin.collections.indices
import kotlin.math.max

/**
 * Legacy PWR reactor core, retrofitted to run its physics on an ECS [World].
 *
 * Public API is preserved for backwards compatibility (regression tests, controller,
 * peripherals). Internally, physics state lives in components attached to a root entity
 * and per-cell entities, and every physics step is a
 * [io.github.symmetricdevs.supercritical.api.fission.ecs.System]. The precompute systems
 * (geometry / eigenvalue / thermal) are driven from [precompute]; the per-tick systems run
 * through [World.update] inside [tick]. The manual cooldown steps ([updatePower],
 * [updateTemperature], ...) each invoke the matching system directly.
 */
class PWRCore(size: Int, val reactorDepth: Int, controlRodInsertion: Double) : ReactorCore {

    // ----- family / structure -----
    override val family = LegacyPWRFamily

    // ----- ECS world -----
    override val world: World = World(family.buildSchedule())
    private val rootEntity: Entity = world.createEntity()

    private val stateComponent: ReactorStateComponent
    private val limitsComponent: ReactorLimitsComponent
    private val controlRodStateComponent: ControlRodStateComponent
    private val neutronicsGlobalsComponent: NeutronicsGlobalsComponent
    private val thermalGlobalsComponent: ThermalGlobalsComponent
    private val latticeGeometryComponent: LatticeGeometryComponent

    init {
        // Ensure this family's component types are resolvable by reified queries. Idempotent:
        // ScritAddon also registers during mod init, and ComponentTypeRegistry.register no-ops
        // when the same ComponentType instance is re-registered, so the test path (which skips
        // mod init) is covered here without disturbing production ordering.
        family.registerComponents(ComponentTypeRegistry)
        world.addComponent(rootEntity, ReactorComponentTypes.REACTOR_STATE, ReactorStateComponent())
        world.addComponent(rootEntity, ReactorComponentTypes.REACTOR_LIMITS, ReactorLimitsComponent())
        world.addComponent(
            rootEntity,
            ReactorComponentTypes.CONTROL_ROD_STATE,
            ControlRodStateComponent(insertion = max(0.001, controlRodInsertion))
        )
        world.addComponent(rootEntity, ReactorComponentTypes.NEUTRONICS_GLOBALS, NeutronicsGlobalsComponent())
        world.addComponent(rootEntity, ReactorComponentTypes.THERMAL_GLOBALS, ThermalGlobalsComponent())
        world.addComponent(
            rootEntity,
            ReactorComponentTypes.LATTICE_GEOMETRY,
            LatticeGeometryComponent(size, reactorDepth, IntArray(size * size) { -1 })
        )
        world.addComponent(rootEntity, ReactorComponentTypes.REACTOR_FAMILY, ReactorFamilyComponent(LegacyPWRFamily))

        world.resources.set(RootEntityResource::class, RootEntityResource(rootEntity))
        world.resources.set(ReactorCoreResource::class, ReactorCoreResource(this))

        stateComponent = world.getComponent(rootEntity, ReactorComponentTypes.REACTOR_STATE)!!
        limitsComponent = world.getComponent(rootEntity, ReactorComponentTypes.REACTOR_LIMITS)!!
        controlRodStateComponent = world.getComponent(rootEntity, ReactorComponentTypes.CONTROL_ROD_STATE)!!
        neutronicsGlobalsComponent = world.getComponent(rootEntity, ReactorComponentTypes.NEUTRONICS_GLOBALS)!!
        thermalGlobalsComponent = world.getComponent(rootEntity, ReactorComponentTypes.THERMAL_GLOBALS)!!
        latticeGeometryComponent = world.getComponent(rootEntity, ReactorComponentTypes.LATTICE_GEOMETRY)!!
    }

    // ----- physics systems -----
    // Precompute systems run only from precompute(); they reset coolantMass / maxTemperature and
    // would clobber per-tick thermal state if they sat in the tick schedule.
    private val geometryRebuild = GeometryRebuildSystem()
    private val neutronicsPrecompute = NeutronicsPrecomputeSystem()
    private val thermalPrecompute = ThermalPrecomputeSystem()
    // Per-tick systems held for the controller's manual cooldown steps (updatePower,
    // updateTemperature, updatePressure, updateNeutronPoisoning, regulateControlRods).
    // PWRSchedule.create() creates SEPARATE instances for the locked-tick path (world.update).
    // This is correct because all system state lives in ECS components (systems are stateless);
    // the dual instances just mean the schedule owns its own copies for the tick loop and PWRCore
    // owns copies for the controller's manual-step API methods. If a future system caches any
    // instance-local mutable state, it must be shared between the two paths.
    private val neutronicsSystem = NeutronicsSystem()
    private val fuelCycleSystem = FuelCycleSystem()
    private val thermalHydraulics = ThermalHydraulicsSystem()
    private val neutronPoisoningSystem = NeutronPoisoningSystem()
    private val controlRodSystem = ControlRodSystem()

    // ----- structure -----
    private val cellEntities: Array<Array<Entity?>> = Array(size) { arrayOfNulls(size) }
    internal val reactorRadius: Double = size.toDouble() / 2 + 1.5

    // ----- ReactorCore API -----
    override val state: ReactorState
        get() = ReactorState(
            neutronFlux = neutronFlux,
            power = power,
            temperature = temperature,
            prevTemperature = prevTemperature,
            pressure = pressure,
            fuelDepletion = fuelDepletion,
            accumulatedHydrogen = accumulatedHydrogen,
            neutronPoisonAmount = neutronPoisonAmount,
            decayProductsAmount = decayProductsAmount,
            isOn = isOn
        )
    override val limits: ReactorLimits
        get() = ReactorLimits(
            maxTemperature = this@PWRCore.maxTemperature,
            maxPressure = this@PWRCore.maxPressure,
            maxPower = this@PWRCore.maxPower
        )
    override val control: ControlMechanism = LegacyControlRodBank(this)

    // ----- mutable physics state (delegated to ECS components) -----
    internal var k by neutronicsGlobalsComponent::k
    override var kEff by neutronicsGlobalsComponent::kEff
    internal var controlRodFactor by neutronicsGlobalsComponent::controlRodFactor
    internal var neutronToPowerConversion by neutronicsGlobalsComponent::neutronToPowerConversion
    internal var decayNeutrons by neutronicsGlobalsComponent::decayNeutrons
    internal var neutronPoisonAmount by stateComponent::neutronPoisonAmount
    internal var decayProductsAmount by stateComponent::decayProductsAmount
    internal var weightedGenerationTime by neutronicsGlobalsComponent::weightedGenerationTime
    var neutronFlux by stateComponent::neutronFlux
    var power by stateComponent::power
    override var temperature by stateComponent::temperature
    override var prevTemperature by stateComponent::prevTemperature
    override var pressure by stateComponent::pressure
    override var fuelDepletion by stateComponent::fuelDepletion
    override var accumulatedHydrogen by stateComponent::accumulatedHydrogen

    // ----- coolant-derived -----
    override var coolantBaseTemperature by thermalGlobalsComponent::coolantBaseTemperature
    internal var coolantBoilingPointStandardPressure by thermalGlobalsComponent::coolantBoilingPointStandardPressure
    override var coolantExitTemperature by thermalGlobalsComponent::coolantExitTemperature
    internal var coolantMass by thermalGlobalsComponent::coolantMass
    internal var structuralMass by thermalGlobalsComponent::structuralMass
    var fuelMass by thermalGlobalsComponent::fuelMass

    // ----- limits / config -----
    override var maxTemperature by limitsComponent::maxTemperature
    override var maxPressure by limitsComponent::maxPressure
    override var maxPower by limitsComponent::maxPower
    override var controlRodInsertion: Double
        get() = controlRodStateComponent.insertion
        set(value) { controlRodStateComponent.insertion = value }
    override var controlRodRegulationOn: Boolean
        get() = controlRodStateComponent.regulationOn
        set(value) { controlRodStateComponent.regulationOn = value }
    override var isOn by stateComponent::isOn

    // ===================== layout =====================

    /**
     * Creates or refreshes the cell entity at (x, y), attaching the shared position / thermal /
     * neutronics components. The type-specific component is added by the calling [setControlRod] /
     * [setModerator] / [setFuelRod] / [setCoolantChannel] method.
     */
    private fun populateCell(
        x: Int, y: Int,
        maxTemperature: Double, thermalConductivity: Double, mass: Double,
        moderationFactor: Double, absorptionFast: Double, absorptionSlow: Double
    ): Entity {
        val existing = cellEntities[x][y]
        val entity = if (existing != null && world.isAlive(existing)) existing else world.createEntity()
        cellEntities[x][y] = entity
        latticeGeometryComponent.setEntityIndexAt(x, y, entity.index)
        world.addComponent(entity, ReactorComponentTypes.POSITION, PositionComponent(x, y))
        world.addComponent(
            entity,
            ReactorComponentTypes.THERMAL_PROPERTIES,
            ThermalPropertiesComponent(maxTemperature, thermalConductivity, mass)
        )
        world.addComponent(
            entity,
            ReactorComponentTypes.NEUTRONICS_PROPERTIES,
            NeutronicsPropertiesComponent(
                moderationFactor = moderationFactor,
                absorptionFast = absorptionFast,
                absorptionSlow = absorptionSlow
            )
        )
        return entity
    }

    override fun setControlRod(
        x: Int, y: Int, hasModeratorTip: Boolean,
        maxTemperature: Double, thermalConductivity: Double, mass: Double
    ): Entity {
        // Control rods neither moderate nor absorb while withdrawn (insertion is applied separately
        // via the control-rod factor); the eigenvalue solver reads insertion, not these factors.
        val entity = populateCell(x, y, maxTemperature, thermalConductivity, mass, 0.0, 0.0, 0.0)
        world.addComponent(
            entity,
            ReactorComponentTypes.CONTROL_ROD,
            ControlRodComponent(hasModeratorTip, 0.0, 0)
        )
        return entity
    }

    override fun setModerator(
        x: Int, y: Int, moderator: ModeratorStats,
        thermalConductivity: Double, mass: Double
    ): Entity {
        val entity = populateCell(
            x, y,
            moderator.maxTemperature.toDouble(), thermalConductivity, mass,
            moderator.moderationFactor, moderator.absorptionFactor, moderator.absorptionFactor
        )
        world.addComponent(entity, ReactorComponentTypes.MODERATOR, ModeratorComponent(moderator))
        return entity
    }

    override fun setFuelRod(
        x: Int, y: Int, fuel: FissionFuelStats,
        maxTemperature: Double, thermalConductivity: Double, mass: Double
    ): Entity {
        val entity = populateCell(x, y, maxTemperature, thermalConductivity, mass, 0.0, 0.0, 0.0)
        // weight 1.0 / thermalProportion 0.0 are the defaults the eigenvalue + thermal precompute
        // systems overwrite (rod weight from the power iteration, thermalProportion from flux share).
        world.addComponent(entity, ReactorComponentTypes.FUEL_ROD, FuelRodComponent(fuel, 1.0, 0.0))
        return entity
    }

    override fun setCoolantChannel(
        x: Int, y: Int, coolant: CoolantStats,
        maxTemperature: Double, thermalConductivity: Double, mass: Double
    ): Entity {
        val entity = populateCell(
            x, y, maxTemperature, thermalConductivity, mass,
            coolant.moderatorFactor, coolant.fastAbsorptionFactor, coolant.slowAbsorptionFactor
        )
        world.addComponent(
            entity,
            ReactorComponentTypes.COOLANT_CHANNEL,
            CoolantChannelComponent(coolant, 0.0, 0.0)
        )
        return entity
    }

    override fun resetFuelDepletion() {
        this.fuelDepletion = 0.0
    }

    override fun resetThermalState() {
        this.isOn = false
        this.temperature = ReactorPhysics.ROOM_TEMPERATURE
        this.pressure = ReactorPhysics.STANDARD_PRESSURE
        this.power = 0.0
    }

    // ===================== physics steps =====================

    override fun precompute() {
        geometryRebuild.update(world, 0.0)
        val cache = world.cache()
        if (cache.fuelRods.isEmpty()) {
            neutronicsGlobalsComponent.k = 0.0
            neutronicsGlobalsComponent.kEff = 0.0
            limitsComponent.maxPower = 0.0
            neutronicsGlobalsComponent.controlRodFactor = 0.0
            thermalPrecompute.prepareInitialConditionsOnly(world)
            return
        }
        neutronicsPrecompute.update(world, 0.0)
        thermalPrecompute.update(world, 0.0)
    }

    override fun tick() {
        if (!this.isOn || world.cache().fuelRods.isEmpty()) return
        world.update(1.0)
    }

    override fun updatePower() {
        neutronicsSystem.update(world, 1.0)
        fuelCycleSystem.update(world, 1.0)
    }

    override fun updateTemperature() {
        thermalHydraulics.updateTemperatureStep(world)
    }

    override fun updatePressure() {
        thermalHydraulics.updatePressureStep(world)
    }

    override fun updateNeutronPoisoning() {
        neutronPoisoningSystem.update(world, 1.0)
    }

    override fun regulateControlRods() {
        controlRodSystem.regulate(world)
    }

    override fun updateControlRodInsertion(insertion: Double) {
        this.controlRodInsertion = max(0.001, insertion)
        this.controlRodFactor = controlRodFactor(world, world.cache(), this.controlRodInsertion)
    }

    // ===================== safety =====================

    override fun turnOff() {
        this.isOn = false
        this.maxPower = 0.0
        this.k = 0.0
        this.kEff = 0.0
        thermalGlobalsComponent.coolantMass = 0.0
        thermalGlobalsComponent.fuelMass = 0.0
        world.cache().clear()
        for (x in cellEntities.indices) {
            for (y in cellEntities[x].indices) {
                val entity = cellEntities[x][y]
                if (entity != null && world.isAlive(entity)) {
                    world.destroyEntity(entity)
                }
                cellEntities[x][y] = null
            }
        }
    }

    // ===================== serialization =====================

    override fun save(tag: CompoundTag): CompoundTag {
        val inner = serializeNBT()
        for (key in inner.allKeys) {
            val value = inner.get(key) ?: continue
            tag.put(key, value)
        }
        tag.putString("Family", "supercritical:pwr")
        tag.putInt("Version", 1)
        return tag
    }

    override fun load(tag: CompoundTag) {
        require(tag.getString("Family") == "supercritical:pwr") { "Unknown reactor family: ${tag.getString("Family")}" }
        tag.getInt("Version")
        deserializeNBT(tag)
    }

    fun serializeNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putDouble("Temperature", temperature)
        tag.putDouble("PrevTemperature", prevTemperature)
        tag.putDouble("Pressure", pressure)
        tag.putDouble("Power", power)
        tag.putDouble("NeutronFlux", neutronFlux)
        tag.putDouble("FuelDepletion", fuelDepletion)
        tag.putDouble("AccumulatedHydrogen", accumulatedHydrogen)
        tag.putDouble("NeutronPoisonAmount", neutronPoisonAmount)
        tag.putDouble("DecayProductsAmount", decayProductsAmount)
        tag.putDouble("ControlRodInsertion", controlRodInsertion)
        tag.putBoolean("IsOn", isOn)
        tag.putBoolean("ControlRodRegulationOn", controlRodRegulationOn)
        return tag
    }

    fun deserializeNBT(tag: CompoundTag) {
        temperature = tag.getDouble("Temperature")
        prevTemperature = tag.getDouble("PrevTemperature")
        pressure = tag.getDouble("Pressure")
        power = tag.getDouble("Power")
        neutronFlux = tag.getDouble("NeutronFlux")
        fuelDepletion = tag.getDouble("FuelDepletion")
        accumulatedHydrogen = tag.getDouble("AccumulatedHydrogen")
        neutronPoisonAmount = tag.getDouble("NeutronPoisonAmount")
        decayProductsAmount = tag.getDouble("DecayProductsAmount")
        controlRodInsertion = tag.getDouble("ControlRodInsertion")
        isOn = tag.getBoolean("IsOn")
        controlRodRegulationOn = tag.getBoolean("ControlRodRegulationOn")
    }
}