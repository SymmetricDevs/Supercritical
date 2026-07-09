package supercritical.api.nuclear.fission;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import supercritical.api.capability.ICoolantHandler;
import supercritical.api.nuclear.fission.components.ControlRod;
import supercritical.api.nuclear.fission.components.CoolantChannel;
import supercritical.api.nuclear.fission.components.FuelRod;
import supercritical.api.nuclear.fission.components.ReactorComponent;
import supercritical.common.SCConfigHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FissionReactor {

    public static final double R = 8.31446261815324;
    public static final double STANDARD_PRESSURE = 101325;
    public static final double ROOM_TEMPERATURE = 273;
    public static final double AIR_BOILING_POINT = 78.8;

    public static double thermalConductivity = 45; // W/(m K), for steel
    public static double wallThickness = 0.1; // m
    public static double coolantWallThickness = 0.06; // m
    public static double specificHeatCapacity = 420; // J/(kg K), for steel
    public static double convectiveHeatTransferCoefficient = 10; // W/(m^2 K), for slow-moving air
    public static double powerDefectCoefficient = 0.016; // reactivity units
    public static double decayProductRate = 0.997; // based on the half-life of xenon-135, using real-life days as Minecraft days
    public static double poisonFraction = 0.063; // xenon-135 yield from fission
    public static double crossSectionRatio = 4; // ratio between the cross section for typical fuels and xenon-135
    public static double zircaloyHydrogenReactionTemperature = 1500; // K

    private final ReactorComponent[][] reactorLayout;
    private final List<FuelRod> fuelRods = new ArrayList<>();
    private final List<ControlRod> controlRods = new ArrayList<>();
    private final List<CoolantChannel> coolantChannels = new ArrayList<>();
    private final List<ControlRod> effectiveControlRods = new ArrayList<>();
    private final int reactorDepth;
    private final double reactorRadius;
    private final double surfaceArea;
    private final double exteriorPressure = STANDARD_PRESSURE;
    private final double envTemperature = ROOM_TEMPERATURE;

    private double k;
    private double controlRodFactor;
    private double coolantBoilingPointStandardPressure;
    private double coolantExitTemperature;
    private double coolantHeatOfVaporization;
    private double coolantBaseTemperature;
    private double prevTemperature;
    private double neutronPoisonAmount;
    private double decayProductsAmount;
    private double decayNeutrons;
    private double neutronToPowerConversion;
    private double structuralMass;
    private double coolantMass;
    private double weightedGenerationTime = 2;
    private boolean on;

    public double kEff;
    public double controlRodInsertion;
    public double power;
    public double temperature = ROOM_TEMPERATURE;
    public double pressure = STANDARD_PRESSURE;
    public double fuelDepletion = -1;
    public double accumulatedHydrogen;
    public double maxTemperature = 2000;
    public double maxPressure = 15000000;
    public double maxPower = 3;
    public double fuelMass;
    public double neutronFlux;
    public boolean controlRodRegulationOn = true;

    public FissionReactor(int size, int depth, double controlRodInsertion) {
        reactorLayout = new ReactorComponent[size][size];
        reactorDepth = depth;
        reactorRadius = (double) size / 2 + 1.5;
        this.controlRodInsertion = Math.max(0.001, controlRodInsertion);
        surfaceArea = (reactorRadius * reactorRadius) * Math.PI * 2 + reactorDepth * reactorRadius * Math.PI * 2;
        structuralMass = reactorDepth * reactorRadius * reactorRadius * Math.PI * 300;
    }

    public void setComponent(int x, int y, ReactorComponent component) {
        reactorLayout[x][y] = component;
    }

    public ReactorComponent getComponent(int x, int y) {
        return reactorLayout[x][y];
    }

    public void prepareThermalProperties() {
        fuelRods.clear();
        controlRods.clear();
        coolantChannels.clear();
        effectiveControlRods.clear();
        structuralMass = reactorDepth * reactorRadius * reactorRadius * Math.PI * 300;
        fuelMass = 0;
        coolantMass = 0;
        maxTemperature = 2000;

        int fuelIndex = 0;
        int controlIndex = 0;
        int coolantIndex = 0;
        for (int x = 0; x < reactorLayout.length; x++) {
            for (int y = 0; y < reactorLayout[x].length; y++) {
                ReactorComponent component = reactorLayout[x][y];
                if (component == null || !component.isValid()) continue;
                component.setPos(x, y);
                maxTemperature = Math.min(maxTemperature, component.getMaxTemperature());
                structuralMass += component.getMass();
                if (component instanceof FuelRod fuelRod) {
                    fuelRod.setIndex(fuelIndex++);
                    fuelRods.add(fuelRod);
                    fuelMass += fuelRod.getMass();
                } else if (component instanceof ControlRod controlRod) {
                    controlRod.setIndex(controlIndex++);
                    controlRods.add(controlRod);
                } else if (component instanceof CoolantChannel coolantChannel) {
                    coolantChannel.setIndex(coolantIndex++);
                    coolantChannel.setWeight(0);
                    coolantChannels.add(coolantChannel);
                }
            }
        }
    }

    public double computeK(boolean addToEffectiveLists, boolean controlRodsInserted) {
        double[][] geometricMatrixNeutrons = new double[fuelRods.size()][fuelRods.size()];
        double[][] geometricMatrixFastNeutrons = new double[fuelRods.size()][fuelRods.size()];
        double[][] geometricMatrixSlowNeutrons = new double[fuelRods.size()][fuelRods.size()];

        for (int i = 0; i < fuelRods.size(); i++) {
            for (int j = 0; j < i; j++) {
                double mij = 0;
                double saij = 0;
                double faij = 0;
                FuelRod rodOne = fuelRods.get(i);
                FuelRod rodTwo = fuelRods.get(j);

                int prevX = fuelRods.get(i).getX();
                int prevY = fuelRods.get(i).getY();
                int resolution = SCConfigHolder.NUCLEAR.fissionReactorResolution.get().intValue();
                for (int t = 0; t < resolution; t++) {
                    int x = (int) Math.round((rodTwo.getX() - rodOne.getX()) *
                            ((double) t / resolution) + fuelRods.get(i).getX());
                    int y = (int) Math.round((rodTwo.getY() - rodOne.getY()) *
                            ((double) t / resolution) + fuelRods.get(i).getY());
                    if (x < 0 || x > reactorLayout.length - 1 || y < 0 || y > reactorLayout.length - 1) {
                        continue;
                    }
                    ReactorComponent component = reactorLayout[x][y];

                    if (component == null) {
                        continue;
                    }

                    if (!component.samePositionAs(fuelRods.get(i)) &&
                            !component.samePositionAs(fuelRods.get(j))) {
                        saij += component.getAbsorptionFactor(controlRodsInserted, true);
                        faij += component.getAbsorptionFactor(controlRodsInserted, false);
                    }

                    if (component.getModerationFactor() > 0) {
                        mij += component.getModerationFactor();
                        saij = (faij + saij) / 2;
                    }

                    if (!addToEffectiveLists || (x == prevX && y == prevY)) {
                        continue;
                    }
                    prevX = x;
                    prevY = y;

                    if (component instanceof ControlRod controlRod) {
                        controlRod.addFuelRodPair();
                    }
                }

                mij /= resolution;
                faij /= resolution;
                saij /= resolution;

                double dist = rodOne.getDistance(rodTwo);
                double unabsorbedFast = Math.exp(-faij * dist) / dist;
                double unabsorbedSlow = Math.exp(-saij * dist) / dist;
                double fast = Math.exp(-mij * dist) / dist;
                double slow = (1 / dist - fast) * unabsorbedSlow;
                fast = fast * unabsorbedFast;

                double slowNeutronFissionMultiplier = rodTwo.getFuel().getSlowFissionMultiplier();
                double fastNeutronFissionMultiplier = rodTwo.getFuel().getFastFissionMultiplier();
                geometricMatrixNeutrons[i][j] = slow * slowNeutronFissionMultiplier +
                        fast * fastNeutronFissionMultiplier;

                slowNeutronFissionMultiplier = rodOne.getFuel().getSlowFissionMultiplier();
                fastNeutronFissionMultiplier = rodOne.getFuel().getFastFissionMultiplier();
                geometricMatrixNeutrons[j][i] = slow * slowNeutronFissionMultiplier +
                        fast * fastNeutronFissionMultiplier;

                if (addToEffectiveLists) {
                    geometricMatrixFastNeutrons[i][j] = fast * rodTwo.getFuel().getFastNeutronCaptureCrossSection();
                    geometricMatrixSlowNeutrons[i][j] = slow * rodTwo.getFuel().getSlowNeutronCaptureCrossSection();

                    geometricMatrixFastNeutrons[j][i] = fast * rodOne.getFuel().getFastNeutronCaptureCrossSection();
                    geometricMatrixSlowNeutrons[j][i] = slow * rodOne.getFuel().getSlowNeutronCaptureCrossSection();
                }
            }
        }

        double[] vector = new double[fuelRods.size()];
        Arrays.fill(vector, 1);
        for (int i = 0; i < SCConfigHolder.NUCLEAR.fissionReactorPowerIterations.get(); i++) {
            normalize(vector);
            multiply(geometricMatrixNeutrons, vector);
        }
        double kCalc = getMagnitude(vector);
        if (addToEffectiveLists) {
            linearNormalize(vector);
            for (int i = 0; i < fuelRods.size(); i++) {
                fuelRods.get(i).setWeight(vector[i]);
            }
            double[] fastVector = Arrays.copyOf(vector, vector.length);
            double[] slowVector = Arrays.copyOf(vector, vector.length);
            multiply(geometricMatrixFastNeutrons, fastVector);
            multiply(geometricMatrixSlowNeutrons, slowVector);
            for (int i = 0; i < fuelRods.size(); i++) {
                if (slowVector[i] + fastVector[i] == 0) {
                    fuelRods.get(i).setThermalProportion(0);
                } else {
                    fuelRods.get(i).setThermalProportion(slowVector[i] / (slowVector[i] + fastVector[i]));
                }
            }
        }

        kCalc *= reactorDepth / (1. + reactorDepth);
        return kCalc;
    }

    public void computeGeometry() {
        effectiveControlRods.clear();

        if (fuelRods.isEmpty()) {
            k = 0;
            kEff = 0;
            maxPower = 0;
            controlRodFactor = 0;
            prepareInitialConditions();
            return;
        }

        k = computeK(true, false);
        double kExperimental = computeK(false, true);

        computeControlRodWeights(((k - 1) / k) - ((kExperimental - 1) / kExperimental));

        neutronToPowerConversion = 0;
        decayNeutrons = 0;

        for (FuelRod rod : fuelRods) {
            neutronToPowerConversion += rod.getFuel().getReleasedHeatEnergy() / rod.getFuel().getRequiredNeutrons();
            decayNeutrons += rod.getFuel().getDecayRate();
        }
        computeCoolantWeights();

        if (fuelRods.size() > 1) {
            neutronToPowerConversion /= fuelRods.size();
            maxPower = calculateMaxPower();
        } else {
            k = 0.00001;
            maxPower = 0.1 * SCConfigHolder.NUCLEAR.nuclearPowerMultiplier.get();
        }

        controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, controlRodInsertion);

        prepareInitialConditions();
    }

    private final int[] dx = { 0, 1, 0, -1 };
    private final int[] dy = { 1, 0, -1, 0 };

    protected void computeCoolantWeights() {
        for (FuelRod rod : fuelRods) {
            for (int i = 0; i < 4; i++) {
                int x = rod.getX() + dx[i];
                int y = rod.getY() + dy[i];
                if (x < 0 || x >= reactorLayout.length || y < 0 || y >= reactorLayout[x].length) continue;
                ReactorComponent comp = reactorLayout[x][y];
                if (comp instanceof CoolantChannel coolantChannel) {
                    coolantChannel.addWeight(rod.getWeight());
                }
            }
        }
    }

    protected void computeControlRodWeights(double totalWorth) {
        double totalWeight = 0;
        for (ControlRod rod : controlRods) {
            rod.computeWeightFromFuelRodMap();
            if (rod.getWeight() > 0) {
                effectiveControlRods.add(rod);
                totalWeight += rod.getWeight();
            }
        }
        ControlRod.normalizeWeights(effectiveControlRods, totalWeight, totalWorth);
    }

    public void resetFuelDepletion() {
        this.fuelDepletion = 0;
    }

    public void prepareInitialConditions() {
        coolantBaseTemperature = 0;
        coolantBoilingPointStandardPressure = 0;
        coolantExitTemperature = 0;
        coolantHeatOfVaporization = 0;
        weightedGenerationTime = 0;

        for (FuelRod rod : fuelRods) {
            weightedGenerationTime += rod.getNeutronGenerationTime();
        }
        if (fuelRods.isEmpty()) {
            weightedGenerationTime = 2;
        } else {
            weightedGenerationTime /= fuelRods.size();
        }

        for (CoolantChannel channel : coolantChannels) {
            ICoolantStats prop = channel.getCoolant();
            var original = CoolantRegistry.originalFluid(prop);

            if (original != null) {
                coolantBaseTemperature += original.getFluidType().getTemperature();
            }
            coolantBoilingPointStandardPressure += prop.getBoilingPoint();
            coolantExitTemperature += prop.getHotCoolant().getFluidType().getTemperature();
            coolantHeatOfVaporization += prop.getHeatOfVaporization();
        }

        if (!coolantChannels.isEmpty()) {
            coolantBaseTemperature /= coolantChannels.size();
            coolantBoilingPointStandardPressure /= coolantChannels.size();
            coolantExitTemperature /= coolantChannels.size();
            coolantHeatOfVaporization /= coolantChannels.size();

            if (coolantBaseTemperature == 0) {
                coolantBaseTemperature = envTemperature;
            }
            if (coolantBoilingPointStandardPressure == 0) {
                coolantBoilingPointStandardPressure = AIR_BOILING_POINT;
            }
        }
        on = true;
    }

    public double makeCoolantFlow() {
        double heatRemoved = 0;
        coolantMass = 0;
        for (CoolantChannel channel : coolantChannels) {
            ICoolantHandler input = channel.getInputHandler();
            ICoolantHandler output = channel.getOutputHandler();
            if (input == null || output == null) continue;

            var inputTank = input.getFluidTank();
            var outputTank = output.getFluidTank();
            FluidStack drained = inputTank.drain(16000, FluidAction.SIMULATE);
            if (drained.isEmpty()) continue;

            int available = drained.getAmount();
            ICoolantStats prop = channel.getCoolant();
            var original = CoolantRegistry.originalFluid(prop);
            int coolantTemp = original == null ? (int) ROOM_TEMPERATURE : original.getFluidType().getTemperature();
            var hotCoolant = prop.getHotCoolant();
            int cooledTemperature = hotCoolant == null ? (int) ROOM_TEMPERATURE : hotCoolant.getFluidType().getTemperature();
            if (cooledTemperature > this.temperature) continue;

            double heatRemovedPerLiter = prop.getSpecificHeatCapacity() /
                    SCConfigHolder.NUCLEAR.fissionCoolantDivisor.get() *
                    (cooledTemperature - coolantTemp);
            if (heatRemovedPerLiter <= 0) continue;

            double heatFluxPerAreaAndTemp = 1 /
                    (1 / prop.getCoolingFactor() + coolantWallThickness / thermalConductivity);
            double idealHeatFlux = heatFluxPerAreaAndTemp * channel.getWeight() * reactorDepth *
                    (temperature - cooledTemperature);

            double idealFluidUsed = idealHeatFlux / heatRemovedPerLiter;
            double cappedFluidUsed = Math.min(available, idealFluidUsed);

            int remainingSpace = outputTank.getTankCapacity(0) - outputTank.getFluidInTank(0).getAmount();
            int actualFlowRate = Math.min(remainingSpace,
                    (int) (cappedFluidUsed + channel.partialCoolant));
            channel.partialCoolant += cappedFluidUsed - actualFlowRate;

            FluidStack hotFluid = new FluidStack(hotCoolant, actualFlowRate);
            inputTank.drain(actualFlowRate, FluidAction.EXECUTE);
            outputTank.fill(hotFluid, FluidAction.EXECUTE);

            if (prop.accumulatesHydrogen() &&
                    this.temperature > zircaloyHydrogenReactionTemperature) {
                double boilingPoint = coolantBoilingPoint(prop);
                if (this.temperature > boilingPoint) {
                    this.accumulatedHydrogen += (this.temperature - boilingPoint) / boilingPoint;
                } else if (actualFlowRate < Math.min(remainingSpace, idealFluidUsed)) {
                    this.accumulatedHydrogen += (this.temperature - zircaloyHydrogenReactionTemperature) /
                            zircaloyHydrogenReactionTemperature;
                }
            }

            this.coolantMass += cappedFluidUsed * prop.getMass();
            heatRemoved += cappedFluidUsed * heatRemovedPerLiter;
        }
        this.coolantMass /= 1000;
        this.accumulatedHydrogen *= 0.98;
        return heatRemoved;
    }

    public double calculateMaxPower() {
        double hypotheticalTemperature = Math.min(maxTemperature, zircaloyHydrogenReactionTemperature);
        double heatRemoved = 0;
        for (CoolantChannel channel : coolantChannels) {
            ICoolantStats prop = channel.getCoolant();
            var original = CoolantRegistry.originalFluid(prop);
            int coolantTemp = original == null ? (int) ROOM_TEMPERATURE : original.getFluidType().getTemperature();

            var hotCoolant = prop.getHotCoolant();
            int cooledTemperature = hotCoolant == null ? (int) ROOM_TEMPERATURE : hotCoolant.getFluidType().getTemperature();
            if (cooledTemperature > hypotheticalTemperature) {
                continue;
            }

            double heatRemovedPerLiter = prop.getSpecificHeatCapacity() /
                    SCConfigHolder.NUCLEAR.fissionCoolantDivisor.get() *
                    (cooledTemperature - coolantTemp);

            double heatFluxPerAreaAndTemp = 1 /
                    (1 / prop.getCoolingFactor() + coolantWallThickness / thermalConductivity);
            double idealHeatFlux = heatFluxPerAreaAndTemp * channel.getWeight() * reactorDepth *
                    (hypotheticalTemperature - cooledTemperature);

            double idealFluidUsed = idealHeatFlux / heatRemovedPerLiter;

            heatRemoved += idealFluidUsed * heatRemovedPerLiter;
        }
        double timeConstant = specificHeatCapacity *
                (1 / convectiveHeatTransferCoefficient + wallThickness / thermalConductivity) / this.surfaceArea;

        return ((hypotheticalTemperature - envTemperature) * (timeConstant * (this.coolantMass +
                this.structuralMass + this.fuelMass)) + heatRemoved) / 1e6;
    }

    protected double coolantBoilingPoint() {
        return this.coolantBoilingPointStandardPressure;
    }

    protected double coolantBoilingPoint(ICoolantStats coolant) {
        if (coolant.getBoilingPoint() == 0) {
            return coolantBoilingPoint();
        }
        return coolant.getBoilingPoint();
    }

    public void updateTemperature() {
        this.prevTemperature = this.temperature;
        this.temperature = responseFunctionTemperature(envTemperature, this.temperature, this.power * 1e6, 0);
        this.temperature = Math.min(maxTemperature, temperature);
        double heatRemoved = this.makeCoolantFlow();
        this.temperature = responseFunctionTemperature(envTemperature, prevTemperature, this.power * 1e6, heatRemoved);
        this.temperature = Math.max(this.temperature, this.coolantBaseTemperature);
    }

    public void updatePressure() {
        this.pressure = responseFunction(
                !(this.temperature <= this.coolantBoilingPoint()) && this.on ? 1000. * R * this.temperature :
                        this.exteriorPressure,
                this.pressure, 0.2);
    }

    public void updateNeutronPoisoning() {
        this.decayProductsAmount *= decayProductRate;
        this.neutronPoisonAmount += this.decayProductsAmount * (1 - decayProductRate) * poisonFraction;
        this.neutronPoisonAmount *= decayProductRate * Math.exp(-crossSectionRatio * power / surfaceArea);
    }

    public double getTotalDecayNeutrons() {
        return this.neutronPoisonAmount * 0.05 + this.decayProductsAmount * 0.1 + this.decayNeutrons;
    }

    public void updatePower() {
        if (this.on) {
            this.neutronFlux += getTotalDecayNeutrons();
            this.kEff = 1 / ((1 / this.k) + powerDefectCoefficient * (this.power / this.maxPower) +
                    neutronPoisonAmount * crossSectionRatio / surfaceArea + controlRodFactor);
            this.kEff = Math.max(0, this.kEff);

            double inverseReactorPeriod = (this.kEff - 1) / weightedGenerationTime;

            this.neutronFlux *= Math.exp(inverseReactorPeriod);

            this.fuelDepletion += this.neutronFlux * reactorDepth;
            this.decayProductsAmount += Math.max(neutronFlux, 0.) / 250000;

            this.power = this.neutronFlux * this.neutronToPowerConversion;
        } else {
            this.neutronFlux *= 0.5;
            this.power *= 0.5;
        }
    }

    public boolean checkForMeltdown() {
        return this.temperature > this.maxTemperature;
    }

    public boolean checkForExplosion() {
        return this.pressure > this.maxPressure;
    }

    public void tick() {
        if (!on || fuelRods.isEmpty()) return;
        updatePower();
        updateTemperature();
        updatePressure();
        updateNeutronPoisoning();
        regulateControlRods();
    }

    protected static double responseFunction(double target, double current, double criticalRate) {
        if (current < 0) current = criticalRate < 1 ? 0 : 0.1;
        double expDecay = Math.exp(-criticalRate);
        return current * expDecay + target * (1 - expDecay);
    }

    protected double responseFunctionTemperature(double envTemperature, double currentTemperature, double heatAdded,
                                                 double heatAbsorbed) {
        currentTemperature = Math.max(0.1, currentTemperature);
        heatAbsorbed = Math.max(0, heatAbsorbed);
        double timeConstant = specificHeatCapacity *
                (1 / convectiveHeatTransferCoefficient + wallThickness / thermalConductivity) / this.surfaceArea;
        double expDecay = Math.exp(-timeConstant);
        double effectiveEnvTemperature = envTemperature +
                (heatAdded - heatAbsorbed) / (timeConstant * (this.coolantMass + this.structuralMass + this.fuelMass));
        return currentTemperature * expDecay + effectiveEnvTemperature * (1 - expDecay);
    }

    public static double getMagnitude(double[] vector) {
        double magnitude = 0;
        for (double component : vector) magnitude += component * component;
        return Math.sqrt(magnitude);
    }

    public static void normalize(double[] vector) {
        double magnitude = getMagnitude(vector);
        if (magnitude == 0) return;
        for (int i = 0; i < vector.length; i++) vector[i] /= magnitude;
    }

    public static void linearNormalize(double[] vector) {
        double sum = 0;
        for (double component : vector) sum += component;
        if (sum == 0) return;
        for (int i = 0; i < vector.length; i++) vector[i] /= sum;
    }

    public static void multiply(double[][] matrix, double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        System.arraycopy(result, 0, vector, 0, result.length);
    }

    public void updateControlRodInsertion(double controlRodInsertion) {
        this.controlRodInsertion = Math.max(0.001, controlRodInsertion);
        this.controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion);
    }

    public void regulateControlRods() {
        if (!this.on || !this.controlRodRegulationOn) return;

        boolean adjustFactor = false;
        if (pressure > maxPressure * 0.8 || temperature > (coolantExitTemperature + maxTemperature) / 2 ||
                temperature > maxTemperature - 150 || temperature - prevTemperature > 30) {
            if (kEff > 0.99) {
                this.controlRodInsertion += 0.004;
                adjustFactor = true;
            }
        } else if (temperature > coolantExitTemperature * 0.3 + coolantBaseTemperature * 0.7) {
            if (kEff > 1.01) {
                this.controlRodInsertion += 0.008;
                adjustFactor = true;
            } else if (kEff < 1.005) {
                this.controlRodInsertion -= 0.001;
                adjustFactor = true;
            }
        } else if (temperature > coolantExitTemperature * 0.1 + coolantBaseTemperature * 0.9) {
            if (kEff > 1.025) {
                this.controlRodInsertion += 0.012;
                adjustFactor = true;
            } else if (kEff < 1.015) {
                this.controlRodInsertion -= 0.004;
                adjustFactor = true;
            }
        } else {
            if (kEff > 1.1) {
                this.controlRodInsertion += 0.02;
                adjustFactor = true;
            } else if (kEff < 1.05) {
                this.controlRodInsertion -= 0.006;
                adjustFactor = true;
            }
        }

        if (adjustFactor) {
            this.controlRodInsertion = Math.max(0, Math.min(1, this.controlRodInsertion));
            this.controlRodFactor = ControlRod.controlRodFactor(effectiveControlRods, this.controlRodInsertion);
        }
    }

    public void turnOff() {
        this.on = false;
        this.maxPower = 0;
        this.k = 0;
        this.kEff = 0;
        this.coolantMass = 0;
        this.fuelMass = 0;
        for (ReactorComponent[] components : reactorLayout) {
            Arrays.fill(components, null);
        }
        fuelRods.clear();
        controlRods.clear();
        coolantChannels.clear();
        effectiveControlRods.clear();
    }

    public List<FuelRod> getFuelRods() { return fuelRods; }
    public List<ControlRod> getControlRods() { return controlRods; }
    public List<CoolantChannel> getCoolantChannels() { return coolantChannels; }
    public int getReactorDepth() { return reactorDepth; }
    public boolean isOn() { return on; }
    public void setOn(boolean on) { this.on = on; }
}
