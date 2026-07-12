package io.github.symmetricdevs.supercritical.api.nuclear.reactor.pwr

import io.github.symmetricdevs.supercritical.api.nuclear.fission.CoolantRegistry
import io.github.symmetricdevs.supercritical.api.nuclear.fission.FissionReactor
import io.github.symmetricdevs.supercritical.api.nuclear.fission.ICoolantStats
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.HeatTransferResult
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.NeutronicsResult
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorGeometry
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ReactorState
import io.github.symmetricdevs.supercritical.api.nuclear.reactor.ThermalHydraulicsKernel
import io.github.symmetricdevs.supercritical.config.ScritConfig
import net.minecraft.nbt.CompoundTag
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/**
 * Legacy PWR thermal hydraulics: coolant flow, the temperature/pressure response functions,
 * maximum-power derivation, and hydrogen accumulation - all lifted verbatim from the 1.12.2
 * `FissionReactor` physics.
 *
 * As with [LegacyEigenvalueNeutronics], every field read/write goes through the [reactor]
 * reference so the legacy mutation order and every computed value (temperature, prevTemperature,
 * pressure, coolantMass, accumulatedHydrogen, maxPower) is preserved byte-for-byte. The
 * [transfer] result is informational; reactor fields remain authoritative.
 */
class LegacyPWRThermalHydraulics(private val reactor: FissionReactor) : ThermalHydraulicsKernel {

    /**
     * Thermal slice of legacy computeGeometry: assign coolant-channel weights from the (already
     * computed) fuel-rod weights, derive maxPower, then seed the coolant/thermal constants via
     * the reactor's [FissionReactor.prepareInitialConditions].
     */
    override fun precompute(geometry: ReactorGeometry) {
        reactor.computeCoolantWeights()
        if (reactor.fuelRods.size > 1) {
            reactor.maxPower = calculateMaxPower()
        } else {
            reactor.maxPower = 0.1 * ScritConfig.INSTANCE.nuclear.nuclearPowerMultiplier
        }
        reactor.prepareInitialConditions()
    }

    /**
     * Combined temperature + pressure update. Order matches the legacy tick
     * (updateTemperature then updatePressure). Returns the heat removed by the coolant flow for
     * API consumers; the temperature field already incorporates it.
     */
    override fun transfer(state: ReactorState, neutronics: NeutronicsResult, dt: Double): HeatTransferResult {
        val heatRemoved = updateTemperatureStep()
        updatePressureStep()
        return HeatTransferResult(
            heatRemoved = heatRemoved,
            // Legacy has no explicit void-fraction or per-tick hydrogen-produced accounting; the
            // running accumulatedHydrogen total lives on the reactor. These fields are non-load-
            // bearing placeholders for the HeatTransferResult contract.
            voidFraction = 0.0,
            hydrogenProduced = 0.0,
            coolantOutletTemperature = reactor.coolantExitTemperature
        )
    }

    override fun save(tag: CompoundTag) {
        // No kernel-owned persistent state: coolantMass is recomputed every tick in makeCoolantFlow;
        // partialCoolant lives on CoolantChannel instances which are rebuilt on structure reform.
    }

    override fun load(tag: CompoundTag) {
        // See save(): nothing to restore.
    }

    // ----- tick steps (the reactor delegates updateTemperature/updatePressure here) -----

    /** Body of legacy updateTemperature. Returns the coolant heatRemoved for [transfer]. */
    internal fun updateTemperatureStep(): Double {
        reactor.prevTemperature = reactor.temperature
        reactor.temperature = responseFunctionTemperature(reactor.envTemperature, reactor.temperature, reactor.power * 1e6, 0.0)
        reactor.temperature = min(reactor.maxTemperature, reactor.temperature)
        val heatRemoved = makeCoolantFlow()
        reactor.temperature = responseFunctionTemperature(reactor.envTemperature, reactor.prevTemperature, reactor.power * 1e6, heatRemoved)
        reactor.temperature = max(reactor.temperature, reactor.coolantBaseTemperature)
        return heatRemoved
    }

    /** Body of legacy updatePressure. */
    internal fun updatePressureStep() {
        reactor.pressure = FissionReactor.responseFunction(
            if (!(reactor.temperature <= coolantBoilingPoint()) && reactor.isOn) 1000.0 * FissionReactor.R * reactor.temperature else reactor.exteriorPressure,
            reactor.pressure, 0.2
        )
    }

    // ----- coolant flow + heat transfer (moved verbatim from FissionReactor) -----

    private fun makeCoolantFlow(): Double {
        var heatRemoved = 0.0
        reactor.coolantMass = 0.0
        for (channel in reactor.coolantChannels) {
            val input = channel.inputHandler
            val output = channel.outputHandler
            if (input == null || output == null) {
                continue
            }

            val inputTank = input.fluidTank
            val outputTank = output.fluidTank
            val drained = inputTank.drain(16000, FluidAction.SIMULATE)
            if (drained.isEmpty) {
                continue
            }

            val available = drained.amount
            val prop = channel.coolant
            val coolantTemp = coolantInletTemp(prop)
            val hotCoolant = requireNotNull(prop.hotCoolant) {
                "Coolant must define a hot coolant fluid for coolant flow"
            }
            val cooledTemperature = hotCoolant.getFluidType().temperature
            if (cooledTemperature > reactor.temperature) {
                continue
            }

            val heatRemovedPerLiter = heatRemovedPerLiter(prop, coolantTemp, cooledTemperature)
            if (heatRemovedPerLiter <= 0) {
                continue
            }

            val idealFluidUsed =
                idealCoolantHeatFlux(prop, channel.weight, reactor.temperature, cooledTemperature) / heatRemovedPerLiter
            val cappedFluidUsed = min(available.toDouble(), idealFluidUsed)

            val remainingSpace = outputTank.getTankCapacity(0) - outputTank.getFluidInTank(0).amount
            val actualFlowRate = min(
                remainingSpace,
                (cappedFluidUsed + channel.partialCoolant).toInt()
            )
            channel.partialCoolant += cappedFluidUsed - actualFlowRate

            val hotFluid = FluidStack(hotCoolant, actualFlowRate)
            inputTank.drain(actualFlowRate, FluidAction.EXECUTE)
            outputTank.fill(hotFluid, FluidAction.EXECUTE)

            if (prop.accumulatesHydrogen() &&
                reactor.temperature > FissionReactor.zircaloyHydrogenReactionTemperature
            ) {
                val boilingPoint = coolantBoilingPoint(prop)
                if (reactor.temperature > boilingPoint) {
                    reactor.accumulatedHydrogen += (reactor.temperature - boilingPoint) / boilingPoint
                } else if (actualFlowRate < min(remainingSpace.toDouble(), idealFluidUsed)) {
                    reactor.accumulatedHydrogen += (reactor.temperature - FissionReactor.zircaloyHydrogenReactionTemperature) /
                        FissionReactor.zircaloyHydrogenReactionTemperature
                }
            }

            reactor.coolantMass += cappedFluidUsed * prop.mass
            heatRemoved += cappedFluidUsed * heatRemovedPerLiter
        }
        reactor.coolantMass /= 1000.0
        reactor.accumulatedHydrogen *= 0.98
        return heatRemoved
    }

    private fun responseFunctionTemperature(
        envTemperature: Double, currentTemperature: Double, heatAdded: Double,
        heatAbsorbed: Double
    ): Double {
        var currentTemperature = currentTemperature
        var heatAbsorbed = heatAbsorbed
        currentTemperature = max(0.1, currentTemperature)
        heatAbsorbed = max(0.0, heatAbsorbed)
        val timeConstant = thermalTimeConstant()
        val expDecay = exp(-timeConstant)
        val effectiveEnvTemperature = envTemperature +
            (heatAdded - heatAbsorbed) / (timeConstant * (reactor.coolantMass + reactor.structuralMass + reactor.fuelMass))
        return currentTemperature * expDecay + effectiveEnvTemperature * (1 - expDecay)
    }

    private fun calculateMaxPower(): Double {
        val hypotheticalTemperature = min(reactor.maxTemperature, FissionReactor.zircaloyHydrogenReactionTemperature)
        var heatRemoved = 0.0
        for (channel in reactor.coolantChannels) {
            val prop = channel.coolant
            val coolantTemp = coolantInletTemp(prop)

            val hotCoolant = requireNotNull(prop.hotCoolant) {
                "Coolant must define a hot coolant fluid for maximum-power calculation"
            }
            val cooledTemperature = hotCoolant.getFluidType().temperature
            if (cooledTemperature > hypotheticalTemperature) {
                continue
            }

            val heatRemovedPerLiter = heatRemovedPerLiter(prop, coolantTemp, cooledTemperature)
            val idealFluidUsed =
                idealCoolantHeatFlux(prop, channel.weight, hypotheticalTemperature, cooledTemperature) / heatRemovedPerLiter

            heatRemoved += idealFluidUsed * heatRemovedPerLiter
        }
        val timeConstant = thermalTimeConstant()

        return ((hypotheticalTemperature - reactor.envTemperature) * (timeConstant * (reactor.coolantMass +
            reactor.structuralMass + reactor.fuelMass)) + heatRemoved) / 1e6
    }

    private fun coolantBoilingPoint(): Double {
        return reactor.coolantBoilingPointStandardPressure
    }

    private fun coolantBoilingPoint(coolant: ICoolantStats): Double {
        if (coolant.boilingPoint == 0.0) {
            return coolantBoilingPoint()
        }
        return coolant.boilingPoint
    }

    private fun coolantInletTemp(prop: ICoolantStats): Int {
        val original = CoolantRegistry.originalFluid(prop)
        return if (original == null) FissionReactor.ROOM_TEMPERATURE.toInt() else original.getFluidType().temperature
    }

    private fun heatRemovedPerLiter(prop: ICoolantStats, coolantTemp: Int, cooledTemperature: Int): Double =
        prop.specificHeatCapacity / ScritConfig.INSTANCE.nuclear.fissionCoolantDivisor * (cooledTemperature - coolantTemp)

    private fun coolantHeatFluxPerArea(prop: ICoolantStats): Double =
        1 / (1 / prop.coolingFactor + FissionReactor.coolantWallThickness / FissionReactor.thermalConductivity)

    private fun idealCoolantHeatFlux(
        prop: ICoolantStats, weight: Double, refTemp: Double, cooledTemperature: Int
    ): Double =
        coolantHeatFluxPerArea(prop) * weight * reactor.reactorDepth * (refTemp - cooledTemperature)

    private fun thermalTimeConstant(): Double =
        FissionReactor.specificHeatCapacity * (1 / FissionReactor.convectiveHeatTransferCoefficient + FissionReactor.wallThickness / FissionReactor.thermalConductivity) / reactor.surfaceArea
}
