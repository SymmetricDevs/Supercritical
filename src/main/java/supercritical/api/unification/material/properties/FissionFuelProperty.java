package supercritical.api.unification.material.properties;

import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import lombok.Getter;
import lombok.Setter;
import supercritical.api.nuclear.fission.IFissionFuelStats;

public class FissionFuelProperty implements IMaterialProperty, IFissionFuelStats {

    // The max temperature the fuel can handle before it liquefies.
    @Getter(onMethod_ = { @Override })
    private int maxTemperature;
    // Scales how long the fuel rod lasts in the reactor.
    @Getter(onMethod_ = { @Override })
    private int duration;
    // How likely it is to absorb a neutron that had touched a moderator.
    @Setter
    @Getter(onMethod_ = { @Override })
    private double slowNeutronCaptureCrossSection;
    // How likely it is to absorb a neutron that has not yet touched a moderator.
    @Setter
    @Getter(onMethod_ = { @Override })
    private double fastNeutronCaptureCrossSection;
    // How likely it is for a moderated neutron to cause fission in this fuel.
    @Setter
    @Getter(onMethod_ = { @Override })
    private double slowNeutronFissionCrossSection;
    // How likely it is for a not-yet-moderated neutron to cause fission in this fuel.
    @Setter
    @Getter(onMethod_ = { @Override })
    private double fastNeutronFissionCrossSection;
    // The average time for a neutron to be emitted during a fission event. Do not make this accurate.
    @Setter
    @Getter(onMethod_ = { @Override })
    private double neutronGenerationTime;
    private final String id;

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

    public void setMaxTemperature(int maxTemperature) {
        if (maxTemperature <= 0) throw new IllegalArgumentException("Max temperature must be greater than zero!");
        this.maxTemperature = maxTemperature;
    }

    public void setDuration(int duration) {
        if (duration <= 0) throw new IllegalArgumentException("Fuel duration must be greater than zero!");
        this.duration = duration;
    }

    @Override
    public String getID() {
        return id;
    }
}
