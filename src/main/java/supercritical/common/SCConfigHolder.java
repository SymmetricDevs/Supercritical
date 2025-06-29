package supercritical.common;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;

import supercritical.SCValues;

@Config(modid = SCValues.MODID)
public class SCConfigHolder {

    @Comment("Config options for Supercritical")
    @Name("Nuclear Options")
    public static NuclearOptions nuclear = new NuclearOptions();

    @Comment("Miscellaneous options for Supercritical")
    @Name("Misc Options")
    public static MiscOptions misc = new MiscOptions();

    public static class NuclearOptions {

        @Comment({
                "Nuclear Max Power multiplier for balancing purposes.",
                "Default: 0.1"
        })
        @RangeDouble(min = 0, max = 10000)
        public double nuclearPowerMultiplier = 0.1;

        @Comment({
                "How much the amount of power required to boil a coolant is divided by.",
                "Default: 14"
        })
        @RangeDouble(min = 0.1, max = 1000)
        public double fissionCoolantDivisor = 14;

        @Comment({
                "The level of detail to which fission reactors are analyzed. May cause more lag at higher values.",
                "Default: 100"
        })
        @RangeInt(min = 5, max = 10000)
        public double fissionReactorResolution = 100;

        @Comment({
                "The number of times the neutron multiplication value is calculated. May cause more lag at higher values.",
                "Default: 10"
        })
        public int fissionReactorPowerIterations = 10;


        @Comment({
                "Nuclear coolant heat exchanger recipe efficiency multiplier for balancing purposes.",
                "Default: 0.1"
        })
        @RangeDouble(min = 0, max = 1000)
        public double heatExchangerEfficiencyMultiplier = 0.25;

        @Comment({
                "Whether to enable meltdowns and associated explosions or not.",
                "Default: true"
        })
        public boolean enableMeltdown = true;
    }

    public static class MiscOptions {

        @Comment({
                "Do material modifications, like adding flags or properties.",
                "Modpack devs might want to disable this to reduce conflicts.",
                "Default: true"
        })
        public boolean enableMaterialModifications = true;

        @Comment({
                "Whether to register heat exchanger or not. Useful for SuSy.",
                "Default: true"
        })
        public boolean enableHX = true;

        @Comment({
                "Remove all recipes from Supercritical.",
                "Default: false"
        })
        public boolean disableAllRecipes = false;

        @Comment({
                "Remove all materials from Supercritical, except Corium.",
                "Default: false"
        })
        public boolean disableAllMaterials = false;

        @Comment({
                "Make auto-filled fluid block showup in the JEI / in-world preview.",
                "Default: false"
        })
        @RequiresMcRestart
        public boolean showFluidsForAutoFillingMultiblocks = false;

        @Comment({
                "Allow extended facing for Fission Reactor.",
                "Default: false"
        })
        @RequiresMcRestart
        public boolean allowExtendedFacingForFissionReactor = false;
    }
}
