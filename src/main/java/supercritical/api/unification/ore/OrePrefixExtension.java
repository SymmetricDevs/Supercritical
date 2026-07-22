package supercritical.api.unification.ore;

import gregtech.api.unification.ore.OrePrefix;
import java.util.function.Function;
import lombok.experimental.UtilityClass;

/// Extends [OrePrefix] to include radiation properties. Also see
/// [MixinOrePrefix] and [SCOrePrefix].
public interface OrePrefixExtension {

	Function<Double, Double> getRadiationDamageFunction();

	void setRadiationDamageFunction(Function<Double, Double> function);

	@UtilityClass
	class Handler {

		public Function<Double, Double> getRadiationDamageFunction(OrePrefix prefix) {
			return ((OrePrefixExtension) prefix).getRadiationDamageFunction();
		}

		public void setRadiationDamageFunction(OrePrefix prefix, Function<Double, Double> function) {
			((OrePrefixExtension) prefix).setRadiationDamageFunction(function);
		}
	}
}
