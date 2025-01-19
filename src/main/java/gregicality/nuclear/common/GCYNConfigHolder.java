package gregicality.nuclear.common;

import gregicality.nuclear.GCYNValues;
import net.minecraftforge.common.config.Config;

@Config(modid = GCYNValues.MODID)
public class GCYNConfigHolder {

    @Config.Comment("Config options for Gregicality Nuclear")
    @Config.Name("Nuclear Options")
    public static NuclearOptions nuclear = new NuclearOptions();

    public static class NuclearOptions {

        @Config.Comment({"Nuclear Max Power multiplier for balancing purposes", "Default: 0.1"})
        @Config.RangeDouble(min = 0, max = 10000)
        public double nuclearPowerMultiplier = 0.1;

        @Config.Comment({"How much the amount of power required to boil a coolant is divided by."})
        @Config.RangeDouble(min = 0.1, max = 1000)
        public double fissionCoolantDivisor = 14;

        @Config.Comment({
                "The level of detail to which fission reactors are analyzed. May cause more lag at higher values."})
        @Config.RangeInt(min = 5, max = 10000)
        public double fissionReactorResolution = 100;

        @Config.Comment({"Nuclear coolant heat exchanger recipe efficiency multiplier for balancing purposes",
                "Default: 0.1"})
        @Config.RangeDouble(min = 0, max = 1000)
        public double heatExchangerEfficiencyMultiplier = 0.25;
    }
}
