package io.github.symmetricdevs.supercritical.api.nuclear.reactor.systems

import io.github.symmetricdevs.supercritical.api.nuclear.ecs.System
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.World
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.components.ReactorComponentTypes
import io.github.symmetricdevs.supercritical.api.nuclear.ecs.resources.CoolantInventoryBridge
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorPhysics
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

// ----------------------------------------------------------------------------------
// Per-tick PWR systems. Each replaces a step of the legacy reactor simulation,
// reading/writing live component data. They run in the order defined by
// [io.github.symmetricdevs.supercritical.api.nuclear.reactor.pwr.PWRReactorSchedule].
// ----------------------------------------------------------------------------------

/**
 * Point-kinetics solve, ported from `LegacyEigenvalueNeutronics.solve` (the body of legacy
 * `updatePower` minus the fuel-depletion accumulation, which lives in [FuelCycleSystem]).
 *
 * On: evolves neutron flux via the inverse reactor period and derives power from it.
 * Off: halves flux and power (SCRAM relaxation).
 */
class NeutronicsSystem : System {
    override fun update(world: World, dt: Double) {
        val state = world.state() ?: return
        val ng = world.neutronicsGlobals() ?: return
        val thermal = world.thermalGlobals() ?: return
        val limits = world.limits() ?: return
        if (state.isOn) {
            state.neutronFlux += state.neutronPoisonAmount * 0.05 + state.decayProductsAmount * 0.1 + ng.decayNeutrons
            ng.kEff = 1.0 / ((1.0 / ng.k) +
                ReactorPhysics.powerDefectCoefficient * (state.power / limits.maxPower) +
                state.neutronPoisonAmount * ReactorPhysics.crossSectionRatio / thermal.surfaceArea +
                ng.controlRodFactor)
            ng.kEff = max(0.0, ng.kEff)
            val inverseReactorPeriod = (ng.kEff - 1) / ng.weightedGenerationTime
            state.neutronFlux *= exp(inverseReactorPeriod)
            state.decayProductsAmount += max(state.neutronFlux, 0.0) / 250000.0
            state.power = state.neutronFlux * ng.neutronToPowerConversion
        } else {
            state.neutronFlux *= 0.5
            state.power *= 0.5
        }
    }
}

/**
 * Solid-fuel-rod burnup: `fuelDepletion += flux * depth` per tick, only while on.
 * Ported from `SolidRodFuelCycle.step` (legacy `dt = reactorDepth`).
 */
class FuelCycleSystem : System {
    override fun update(world: World, dt: Double) {
        val state = world.state() ?: return
        if (!state.isOn) return
        val lattice = world.lattice() ?: return
        state.fuelDepletion += state.neutronFlux * lattice.depth
    }
}

/**
 * Temperature + pressure update for one tick, ported from `LegacyPWRThermalHydraulics`
 * (`updateTemperatureStep` then `updatePressureStep`). [update] runs both for the tick
 * schedule; [updateTemperatureStep] / [updatePressureStep] are exposed individually for the
 * controller's manual cooldown path.
 */
class ThermalHydraulicsSystem : System {

    override fun update(world: World, dt: Double) {
        updateTemperatureStep(world)
        updatePressureStep(world)
    }

    /** Body of legacy `updateTemperature`. Returns the coolant heat removed (for parity). */
    fun updateTemperatureStep(world: World): Double {
        val state = world.state() ?: return 0.0
        val thermal = world.thermalGlobals() ?: return 0.0
        val limits = world.limits() ?: return 0.0
        state.prevTemperature = state.temperature
        state.temperature = responseFunctionTemperature(world, thermal.envTemperature, state.temperature, state.power * 1e6, 0.0)
        state.temperature = min(limits.maxTemperature, state.temperature)
        val heatRemoved = makeCoolantFlow(world)
        state.temperature = responseFunctionTemperature(world, thermal.envTemperature, state.prevTemperature, state.power * 1e6, heatRemoved)
        state.temperature = max(state.temperature, thermal.coolantBaseTemperature)
        return heatRemoved
    }

    /** Body of legacy `updatePressure`. */
    fun updatePressureStep(world: World) {
        val state = world.state() ?: return
        val thermal = world.thermalGlobals() ?: return
        val boiling = thermal.coolantBoilingPointStandardPressure
        state.pressure = ReactorPhysics.responseFunction(
            if (!(state.temperature <= boiling) && state.isOn) 1000.0 * ReactorPhysics.R * state.temperature else thermal.exteriorPressure,
            state.pressure,
            0.2
        )
    }

    private fun responseFunctionTemperature(
        world: World, envTemperature: Double, currentTemperature: Double, heatAdded: Double, heatAbsorbed: Double
    ): Double {
        val thermal = world.thermalGlobals() ?: return currentTemperature
        val ct = max(0.1, currentTemperature)
        val ha = max(0.0, heatAbsorbed)
        val timeConstant = thermalTimeConstant(thermal.surfaceArea)
        val expDecay = exp(-timeConstant)
        val effectiveEnvTemperature = envTemperature +
            (heatAdded - ha) / (timeConstant * (thermal.coolantMass + thermal.structuralMass + thermal.fuelMass))
        return ct * expDecay + effectiveEnvTemperature * (1 - expDecay)
    }

    private fun makeCoolantFlow(world: World): Double {
        val bridge = world.resources.get<CoolantInventoryBridge>() ?: return 0.0
        val state = world.state() ?: return 0.0
        val thermal = world.thermalGlobals() ?: return 0.0
        val cache = world.cache()
        val lattice = world.lattice() ?: return 0.0
        var heatRemoved = 0.0
        thermal.coolantMass = 0.0
        for (entity in cache.coolantChannels) {
            val pos = world.getComponent(entity, ReactorComponentTypes.POSITION) ?: continue
            val (input, output) = bridge[pos.x, pos.y] ?: continue
            if (output == null) continue

            val inputTank = input.fluidTank
            val outputTank = output.fluidTank
            val drained = inputTank.drain(16000, FluidAction.SIMULATE)
            if (drained.isEmpty) continue

            val available = drained.amount
            val channel = world.getComponent(entity, ReactorComponentTypes.COOLANT_CHANNEL) ?: continue
            val prop = channel.coolant
            val coolantTemp = coolantInletTemp(prop)
            val hotCoolant = requireNotNull(prop.hotCoolant) { "Coolant must define a hot coolant fluid for coolant flow" }
            val cooledTemperature = hotCoolant.getFluidType().temperature
            if (cooledTemperature > state.temperature) continue

            val heatRemovedPerLiter = heatRemovedPerLiter(prop, coolantTemp, cooledTemperature)
            if (heatRemovedPerLiter <= 0) continue

            val idealFluidUsed =
                idealCoolantHeatFlux(prop, channel.weight, lattice.depth, state.temperature, cooledTemperature) / heatRemovedPerLiter
            val cappedFluidUsed = min(available.toDouble(), idealFluidUsed)

            val remainingSpace = outputTank.getTankCapacity(0) - outputTank.getFluidInTank(0).amount
            val actualFlowRate = min(remainingSpace, (cappedFluidUsed + channel.partialCoolant).toInt())
            channel.partialCoolant += cappedFluidUsed - actualFlowRate

            val hotFluid = FluidStack(hotCoolant, actualFlowRate)
            inputTank.drain(actualFlowRate, FluidAction.EXECUTE)
            outputTank.fill(hotFluid, FluidAction.EXECUTE)

            if (prop.accumulatesHydrogen && state.temperature > ReactorPhysics.zircaloyHydrogenReactionTemperature) {
                val boilingPoint = coolantBoilingPoint(prop, thermal.coolantBoilingPointStandardPressure)
                if (state.temperature > boilingPoint) {
                    state.accumulatedHydrogen += (state.temperature - boilingPoint) / boilingPoint
                } else if (actualFlowRate < min(remainingSpace.toDouble(), idealFluidUsed)) {
                    state.accumulatedHydrogen +=
                        (state.temperature - ReactorPhysics.zircaloyHydrogenReactionTemperature) /
                        ReactorPhysics.zircaloyHydrogenReactionTemperature
                }
            }

            thermal.coolantMass += cappedFluidUsed * prop.mass
            heatRemoved += cappedFluidUsed * heatRemovedPerLiter
        }
        thermal.coolantMass /= 1000.0
        state.accumulatedHydrogen *= 0.98
        return heatRemoved
    }
}

/**
 * Xenon/iodine poisoning evolution, ported from `FissionReactor.updateNeutronPoisoning`.
 */
class NeutronPoisoningSystem : System {
    override fun update(world: World, dt: Double) {
        val state = world.state() ?: return
        val thermal = world.thermalGlobals() ?: return
        state.decayProductsAmount *= ReactorPhysics.decayProductRate
        state.neutronPoisonAmount += state.decayProductsAmount * (1 - ReactorPhysics.decayProductRate) * ReactorPhysics.poisonFraction
        state.neutronPoisonAmount *= ReactorPhysics.decayProductRate * exp(-ReactorPhysics.crossSectionRatio * state.power / thermal.surfaceArea)
    }
}

/**
 * Automatic control-rod regulation, ported from `FissionReactor.regulateControlRods`.
 * Re-evaluates `controlRodFactor` (via the shared [controlRodFactor] helper) whenever the
 * insertion is nudged.
 */
class ControlRodSystem : System {
    override fun update(world: World, dt: Double) {
        regulate(world)
    }

    fun regulate(world: World) {
        val state = world.state() ?: return
        val ng = world.neutronicsGlobals() ?: return
        val thermal = world.thermalGlobals() ?: return
        val limits = world.limits() ?: return
        val controlState = world.controlRodState() ?: return
        val cache = world.cache()
        if (!state.isOn || !controlState.regulationOn) return

        var adjustFactor = false
        val pressure = state.pressure
        val temperature = state.temperature
        val prevTemperature = state.prevTemperature
        val maxPressure = limits.maxPressure
        val maxTemperature = limits.maxTemperature
        val kEff = ng.kEff
        val coolantExitTemperature = thermal.coolantExitTemperature
        val coolantBaseTemperature = thermal.coolantBaseTemperature

        if (pressure > maxPressure * 0.8 || temperature > (coolantExitTemperature + maxTemperature) / 2 || temperature > maxTemperature - 150 || temperature - prevTemperature > 30) {
            if (kEff > 0.99) {
                controlState.insertion += 0.004; adjustFactor = true
            }
        } else if (temperature > coolantExitTemperature * 0.3 + coolantBaseTemperature * 0.7) {
            if (kEff > 1.01) {
                controlState.insertion += 0.008; adjustFactor = true
            } else if (kEff < 1.005) {
                controlState.insertion -= 0.001; adjustFactor = true
            }
        } else if (temperature > coolantExitTemperature * 0.1 + coolantBaseTemperature * 0.9) {
            if (kEff > 1.025) {
                controlState.insertion += 0.012; adjustFactor = true
            } else if (kEff < 1.015) {
                controlState.insertion -= 0.004; adjustFactor = true
            }
        } else {
            if (kEff > 1.1) {
                controlState.insertion += 0.02; adjustFactor = true
            } else if (kEff < 1.05) {
                controlState.insertion -= 0.006; adjustFactor = true
            }
        }

        if (adjustFactor) {
            controlState.insertion = max(0.0, min(1.0, controlState.insertion))
            ng.controlRodFactor = controlRodFactor(world, cache, controlState.insertion)
        }
    }
}
