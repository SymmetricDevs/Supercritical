package supercritical.api.unification.material;

public interface MaterialExtension {

    // Assumes one mole per item and that it's always "starting to decay"
    double getDecaysPerSecond();
}
