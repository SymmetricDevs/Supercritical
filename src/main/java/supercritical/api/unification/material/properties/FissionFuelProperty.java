package supercritical.api.unification.material.properties;

import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import supercritical.api.nuclear.fission.IFissionFuelStats;

public class FissionFuelProperty implements IMaterialProperty, IFissionFuelStats {

    // The max temperature the fuel can handle before it liquefies.
    private int maxTemperature;
    // Scales how long the fuel rod lasts in the reactor.
    private int duration;
    // How likely it is to absorb a neutron that had touched a moderator.
    private double slowNeutronCaptureCrossSection;
    // How likely it is to absorb a neutron that has not yet touched a moderator.
    private double fastNeutronCaptureCrossSection;
    // How likely it is for a moderated neutron to cause fission in this fuel.
    private double slowNeutronFissionCrossSection;
    // How likely it is for a not-yet-moderated neutron to cause fission in this fuel.
    private double fastNeutronFissionCrossSection;
    // The average time for a neutron to be emitted during a fission event. Do not make this accurate.
    private double neutronGenerationTime;
    private double releasedNeutrons;
    private double requiredNeutrons = 1;
    private double releasedHeatEnergy;
    private double decayRate;

    private String id;

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }

    public FissionFuelProperty(int maxTemperature, int duration, double slowNeutronCaptureCrossSection,
                               double fastNeutronCaptureCrossSection, double slowNeutronFissionCrossSection,
                               double fastNeutronFissionCrossSection, double neutronGenerationTime, String id) {
        this.maxTemperature = maxTemperature;
        this.duration = duration;
        this.slowNeutronCaptureCrossSection = slowNeutronCaptureCrossSection;
        this.fastNeutronCaptureCrossSection = fastNeutronCaptureCrossSection;
        this.slowNeutronFissionCrossSection = slowNeutronFissionCrossSection;
        this.fastNeutronFissionCrossSection = fastNeutronFissionCrossSection;
        this.neutronGenerationTime = neutronGenerationTime;
        this.id = id;
    }

    public FissionFuelProperty(int maxTemperature, int duration, String id, double neutronGenerationTime) {
        this.maxTemperature = maxTemperature;
        this.duration = duration;
        this.id = id;
        this.neutronGenerationTime = neutronGenerationTime;
    }

    @Override
    public int getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(int maxTemperature) {
        if (maxTemperature <= 0) throw new IllegalArgumentException("Max temperature must be greater than zero!");
        this.maxTemperature = maxTemperature;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        if (duration <= 0) throw new IllegalArgumentException("Fuel duration must be greater than zero!");
        this.duration = duration;
    }

    // Capture cross-sections
    @Override
    public double getSlowNeutronCaptureCrossSection() {
        return slowNeutronCaptureCrossSection;
    }

    public FissionFuelProperty setSlowNeutronCaptureCrossSection(double slowNeutronCaptureCrossSection) {
        this.slowNeutronCaptureCrossSection = slowNeutronCaptureCrossSection;
        return this;
    }

    @Override
    public double getFastNeutronCaptureCrossSection() {
        return fastNeutronCaptureCrossSection;
    }

    public FissionFuelProperty setFastNeutronCaptureCrossSection(double fastNeutronCaptureCrossSection) {
        this.fastNeutronCaptureCrossSection = fastNeutronCaptureCrossSection;
        return this;
    }

    // Fission cross-sections
    @Override
    public double getSlowNeutronFissionCrossSection() {
        return slowNeutronFissionCrossSection;
    }

    public FissionFuelProperty setSlowNeutronFissionCrossSection(double slowNeutronFissionCrossSection) {
        this.slowNeutronFissionCrossSection = slowNeutronFissionCrossSection;
        return this;
    }

    @Override
    public double getFastNeutronFissionCrossSection() {
        return fastNeutronFissionCrossSection;
    }

    public FissionFuelProperty setFastNeutronFissionCrossSection(double fastNeutronFissionCrossSection) {
        this.fastNeutronFissionCrossSection = fastNeutronFissionCrossSection;
        return this;
    }

    @Override
    public double getReleasedNeutrons() {
        return releasedNeutrons;
    }

    public FissionFuelProperty setReleasedNeutrons(double releasedNeutrons) {
        this.releasedNeutrons = releasedNeutrons;
        return this;
    }

    @Override
    public double getRequiredNeutrons() {
        return requiredNeutrons;
    }

    public FissionFuelProperty setRequiredNeutrons(double requiredNeutrons) {
        this.requiredNeutrons = requiredNeutrons;
        return this;
    }


    @Override
    public double getReleasedHeatEnergy() {
        return releasedHeatEnergy;
    }

    public FissionFuelProperty setReleasedHeatEnergy(double releasedHeatEnergy) {
        this.releasedHeatEnergy = releasedHeatEnergy;
        return this;
    }

    public double getDecayRate() {
        return decayRate;
    }

    public FissionFuelProperty setDecayRate(double decayRate) {
        this.decayRate = decayRate;
        return this;
    }

    @Override
    public double getNeutronGenerationTime() {
        return neutronGenerationTime;
    }

    @Override
    public String getID() {
        return this.id;
    }

    public void setNeutronGenerationTime(double neutronGenerationTime) {
        this.neutronGenerationTime = neutronGenerationTime;
    }


}
