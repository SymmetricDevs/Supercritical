package gregicality.nuclear.api.unification.ore;

import java.util.function.Function;

public interface OrePrefixExtension {

    Function<Double, Double> getDamageFunction();

    void setDamageFunction(Function<Double, Double> function);
}
