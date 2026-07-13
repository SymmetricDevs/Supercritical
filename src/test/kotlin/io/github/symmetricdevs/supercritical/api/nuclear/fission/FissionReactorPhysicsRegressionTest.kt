package io.github.symmetricdevs.supercritical.api.nuclear.fission

import io.github.symmetricdevs.supercritical.config.ScritConfig
import net.minecraft.world.item.ItemStack
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.math.abs

/**
 * Pinned regression test for the legacy PWR physics.
 *
 * After the nuclear-simulation API migration the legacy eigenvalue / point-kinetics / thermal /
 * fuel-cycle logic was decomposed into kernels (`LegacyEigenvalueNeutronics`,
 * `LegacyPWRThermalHydraulics`, `SolidRodFuelCycle`) with reusable `computeK` matrices. This test
 * freezes the exact computed values for a fixed deterministic lattice so any future change to the
 * formulas, the tick order, the matrix-reuse, or the kernel wiring trips a failure.
 *
 * Determinism basis (audited by reading every line of the hot path):
 * - All math is IEEE-754 double (`kotlin.math.exp/max/min`, `Math.PI/round/ceil`); no
 *   `random`, `currentTimeMillis`, threads, or hashing of mutable keys.
 * - The power iteration is seeded deterministically (`Arrays.fill(vector, 1.0)`) with a fixed
 *   iteration count from config; `ArrayList`/`Array` iteration is order-stable.
 * - The layout has no coolant channels, so the `Fluid`/`hotCoolant` code paths (which would need a
 *   Minecraft registry bootstrap) are never entered — this keeps the test a pure JVM unit test.
 *   `calculateMaxPower` still derives a non-zero `maxPower` from structural+fuel mass, so the
 *   `power / maxPower` term in `solve` never divides by zero.
 *
 * Values are pinned with a relative tolerance (1e-9): loose enough to absorb last-bit cross-arch
 * noise in `exp`/`sqrt`/`pow`, tight enough that any real formula or ordering change (which diverges
 * by orders of magnitude) is caught. `accumulatedHydrogen` is asserted at exactly 0.0 — without
 * coolant the hydrogen path is inert, which itself pins that the inert path stays inert.
 *
 * If a change to the physics is INTENTIONAL, regenerate these values by temporarily converting the
 * assertions to print actuals (or re-enable the capture spike), capture the new outputs, and update.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FissionReactorPhysicsRegressionTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun initConfig() {
            // Physics reads ScritConfig.INSTANCE.nuclear.fissionReactorResolution (100.0) and
            // fissionReactorPowerIterations (10). The Forge-dependent ScritConfig.init() cannot run
            // in a unit test, so install a default-configured instance via the internal test hook.
            ScritConfig.setInstanceForTesting(ScritConfig())
        }
    }

    @Test
    fun pinnedLegacyPwrPhysicsAfterFiveTicks() {
        val reactor = buildReferenceReactor()
        reactor.precompute()

        // ----- post-precompute: eigenvalue + thermal setup -----
        assertApprox("precompute.k", 1.2684092590845895, reactor.k)
        assertApprox("precompute.kEff", 0.0, reactor.kEff)
        assertApprox("precompute.neutronFlux", 0.0, reactor.neutronFlux)
        assertApprox("precompute.power", 0.0, reactor.power)
        assertApprox("precompute.temperature", 273.0, reactor.temperature)
        assertApprox("precompute.prevTemperature", 0.0, reactor.prevTemperature)
        assertApprox("precompute.pressure", 101325.0, reactor.pressure)
        assertApprox("precompute.fuelDepletion", -1.0, reactor.fuelDepletion)
        assertApprox("precompute.maxPower", 17.699469251302112, reactor.maxPower)
        assertApprox("precompute.maxTemperature", 2000.0, reactor.maxTemperature)
        assertApprox("precompute.maxPressure", 1.5E7, reactor.maxPressure)
        assertApprox("precompute.controlRodInsertion", 0.5, reactor.controlRodInsertion)

        // ----- tick 1: first kinetics + thermal + pressure + fuel-cycle step -----
        reactor.tick()
        assertApprox("t1.kEff", 1.1520620723162562, reactor.kEff)
        assertApprox("t1.neutronFlux", 0.44267650310250667, reactor.neutronFlux)
        assertApprox("t1.power", 88.53530062050133, reactor.power)
        assertApprox("t1.temperature", 1334.0813621138457, reactor.temperature)
        assertApprox("t1.prevTemperature", 273.0, reactor.prevTemperature)
        assertApprox("t1.pressure", 2093627.1263744992, reactor.pressure)
        assertApprox("t1.fuelDepletion", 1.2133825155125333, reactor.fuelDepletion)
        assertApprox("t1.neutronPoisonAmount", 6.950985089649313E-11, reactor.neutronPoisonAmount)
        assertApprox("t1.decayProductsAmount", 1.7653938943727965E-6, reactor.decayProductsAmount)
        assertApprox("t1.controlRodInsertion", 0.504, reactor.controlRodInsertion)

        // ----- tick 2 -----
        reactor.tick()
        assertApprox("t2.kEff", 1.0540961589312567, reactor.kEff)
        assertApprox("t2.neutronFlux", 0.8736217092141196, reactor.neutronFlux)
        assertApprox("t2.power", 174.72434184282392, reactor.power)
        assertApprox("t2.temperature", 3244.6834691141567, reactor.temperature)
        assertApprox("t2.prevTemperature", 1334.0813621138457, reactor.prevTemperature)
        assertApprox("t2.pressure", 6604362.296811293, reactor.pressure)
        assertApprox("t2.fuelDepletion", 5.581491061583131, reactor.fuelDepletion)
        assertApprox("t2.neutronPoisonAmount", 4.8126030254565255E-11, reactor.neutronPoisonAmount)
        assertApprox("t2.decayProductsAmount", 5.244101089035587E-6, reactor.decayProductsAmount)
        assertApprox("t2.controlRodInsertion", 0.508, reactor.controlRodInsertion)

        // ----- tick 3 (pressure crosses the explosion limit) -----
        reactor.tick()
        assertApprox("t3.kEff", 0.9734914910573619, reactor.kEff)
        assertApprox("t3.neutronFlux", 1.2513120664429134, reactor.neutronFlux)
        assertApprox("t3.power", 250.26241328858268, reactor.power)
        assertApprox("t3.temperature", 5730.289947871492, reactor.temperature)
        assertApprox("t3.prevTemperature", 3244.6834691141567, reactor.prevTemperature)
        assertApprox("t3.pressure", 1.4043637555889659E7, reactor.pressure)
        assertApprox("t3.fuelDepletion", 11.838051393797699, reactor.fuelDepletion)
        assertApprox("t3.neutronPoisonAmount", 2.3616708917332518E-11, reactor.neutronPoisonAmount)
        assertApprox("t3.decayProductsAmount", 1.021860130674282E-5, reactor.decayProductsAmount)
        assertApprox("t3.controlRodInsertion", 0.508, reactor.controlRodInsertion)

        // ----- tick 4 -----
        reactor.tick()
        assertApprox("t4.kEff", 0.9128123664417434, reactor.kEff)
        assertApprox("t4.neutronFlux", 1.558066599736322, reactor.neutronFlux)
        assertApprox("t4.power", 311.6133199472644, reactor.power)
        assertApprox("t4.temperature", 8521.462430316815, reactor.temperature)
        assertApprox("t4.prevTemperature", 5730.289947871492, reactor.prevTemperature)
        assertApprox("t4.pressure", 2.4341134398321502E7, reactor.pressure)
        assertApprox("t4.fuelDepletion", 19.628384392479308, reactor.fuelDepletion)
        assertApprox("t4.neutronPoisonAmount", 1.2593396166156714E-11, reactor.neutronPoisonAmount)
        assertApprox("t4.decayProductsAmount", 1.640151510257104E-5, reactor.decayProductsAmount)
        assertApprox("t4.controlRodInsertion", 0.508, reactor.controlRodInsertion)

        // ----- tick 5 -----
        reactor.tick()
        assertApprox("t5.kEff", 0.8688282210002461, reactor.kEff)
        assertApprox("t5.neutronFlux", 1.7941126291162666, reactor.neutronFlux)
        assertApprox("t5.power", 358.82252582325333, reactor.power)
        assertApprox("t5.temperature", 11395.88826976387, reactor.temperature)
        assertApprox("t5.prevTemperature", 8521.462430316815, reactor.prevTemperature)
        assertApprox("t5.pressure", 3.710422097809735E7, reactor.pressure)
        assertApprox("t5.fuelDepletion", 28.598947538060642, reactor.fuelDepletion)
        assertApprox("t5.neutronPoisonAmount", 7.795180941361257E-12, reactor.neutronPoisonAmount)
        assertApprox("t5.decayProductsAmount", 2.3507231722179E-5, reactor.decayProductsAmount)
        assertApprox("t5.controlRodInsertion", 0.508, reactor.controlRodInsertion)
        // No coolant => the hydrogen-accumulation path is inert and must stay exactly zero.
        assertApprox("t5.accumulatedHydrogen", 0.0, reactor.accumulatedHydrogen)
    }

    /**
     * Build the fixed reference lattice: a 5x5 PWR of depth 5 with four fuel rods, one central
     * moderator, and one control rod on the path between two rods. No coolant channels.
     */
    private fun buildReferenceReactor(): FissionReactor {
        val reactor = FissionReactor(size = 5, reactorDepth = 5, controlRodInsertion = 0.5)
        val fuel = TestFuelStats()
        reactor.setFuelRod(1, 1, fuel, 2000.0, 45.0, 100.0)
        reactor.setFuelRod(1, 3, fuel, 2000.0, 45.0, 100.0)
        reactor.setFuelRod(3, 1, fuel, 2000.0, 45.0, 100.0)
        reactor.setFuelRod(3, 3, fuel, 2000.0, 45.0, 100.0)
        reactor.setModerator(2, 2, TestModeratorStats(), 45.0, 100.0)
        reactor.setControlRod(1, 2, hasModeratorTip = false, 2000.0, 45.0, 100.0)
        return reactor
    }

    /** Relative-tolerance assertion: absorbs last-bit noise, fails on any real divergence. */
    private fun assertApprox(name: String, expected: Double, actual: Double) {
        val tolerance = abs(expected) * 1e-9
        val delta = abs(expected - actual)
        if (delta > tolerance) {
            throw AssertionError("$name: expected=$expected actual=$actual delta=$delta (tolerance=$tolerance)")
        }
    }
}

private class TestFuelStats : IFissionFuelStats {
    override val maxTemperature = 2000
    override val duration = 10000
    override val slowNeutronCaptureCrossSection = 1.0
    override val fastNeutronCaptureCrossSection = 0.5
    override val slowNeutronFissionCrossSection = 2.0
    override val fastNeutronFissionCrossSection = 1.0
    override val releasedNeutrons = 2.5
    override val requiredNeutrons = 1.0
    override val releasedHeatEnergy = 200.0
    override val decayRate = 0.1
    override val neutronGenerationTime = 1.5
    override val id: String = "test_fuel"
    override val depletedFuels: MutableList<ItemStack> = arrayListOf()
    override fun getDepletedFuel(thermalRatio: Double): ItemStack =
        throw UnsupportedOperationException("not exercised by physics")
}

private class TestModeratorStats : IModeratorStats {
    override val maxTemperature = 2000
    override val moderationFactor = 1.0
    override val absorptionFactor = 0.5
}
