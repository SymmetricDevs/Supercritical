package supercritical.common;

import net.minecraftforge.common.config.Config;

import supercritical.SCValues;

@Config(modid = SCValues.MODID)
public class SCConfigHolder {

    @Config.Comment("Config options for Supercritical")
    @Config.Name("Nuclear Options")
    public static NuclearOptions nuclear = new NuclearOptions();

    @Config.Comment("Miscellaneous options for Supercritical")
    @Config.Name("Misc Options")
    public static MiscOptions misc = new MiscOptions();

    public static class NuclearOptions {

        @Config.Comment({ "Nuclear Max Power multiplier for balancing purposes.", "Default: 0.1" })
        @Config.RangeDouble(min = 0, max = 10000)
        public double nuclearPowerMultiplier = 0.1;

        @Config.Comment({ "How much the amount of power required to boil a coolant is divided by." })
        @Config.RangeDouble(min = 0.1, max = 1000)
        public double fissionCoolantDivisor = 14;

        @Config.Comment({
                "The level of detail to which fission reactors are analyzed. May cause more lag at higher values." })
        @Config.RangeInt(min = 5, max = 10000)
        public double fissionReactorResolution = 100;

        @Config.Comment({ "Nuclear coolant heat exchanger recipe efficiency multiplier for balancing purposes.",
                "Default: 0.1" })
        @Config.RangeDouble(min = 0, max = 1000)
        public double heatExchangerEfficiencyMultiplier = 0.25;
    }

    public static class MiscOptions {

        @Config.Comment({ "Do material modifications, like adding flags or properties.",
                "Modpack devs might want to disable this to reduce conflicts." })
        public boolean enableMaterialModifications = true;

        @Config.Comment("Whether to register heat exchanger or not. Useful for SuSy.")
        public boolean enableHX = true;

        @Config.Comment("Remove all recipes in Supercritical")
        public boolean disableAllRecipes = false;
    }
}
