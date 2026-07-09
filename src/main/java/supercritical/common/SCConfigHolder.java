package supercritical.common;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class SCConfigHolder {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final NuclearOptions NUCLEAR;
    public static final MiscOptions MISC;

    static {
        var builder = new ForgeConfigSpec.Builder();
        builder.comment("Config options for Supercritical").push("nuclear");
        NUCLEAR = new NuclearOptions(builder);
        builder.pop();

        builder.comment("Miscellaneous options for Supercritical").push("misc");
        MISC = new MiscOptions(builder);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private SCConfigHolder() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static final class NuclearOptions {

        public final ForgeConfigSpec.DoubleValue nuclearPowerMultiplier;
        public final ForgeConfigSpec.DoubleValue fissionCoolantDivisor;
        public final ForgeConfigSpec.DoubleValue fissionReactorResolution;
        public final ForgeConfigSpec.IntValue fissionReactorPowerIterations;
        public final ForgeConfigSpec.DoubleValue heatExchangerEfficiencyMultiplier;
        public final ForgeConfigSpec.BooleanValue enableMeltdown;

        private NuclearOptions(ForgeConfigSpec.Builder builder) {
            nuclearPowerMultiplier = builder
                    .comment("Nuclear Max Power multiplier for balancing purposes.", "Default: 0.1")
                    .defineInRange("nuclearPowerMultiplier", 0.1D, 0D, 10000D);
            fissionCoolantDivisor = builder
                    .comment("How much the amount of power required to boil a coolant is divided by.", "Default: 14")
                    .defineInRange("fissionCoolantDivisor", 14D, 0.1D, 1000D);
            fissionReactorResolution = builder
                    .comment("The level of detail to which fission reactors are analyzed. May cause more lag at higher values.",
                            "Default: 100")
                    .defineInRange("fissionReactorResolution", 100D, 5D, 10000D);
            fissionReactorPowerIterations = builder
                    .comment("The number of times the neutron multiplication value is calculated. May cause more lag at higher values.",
                            "Default: 10")
                    .defineInRange("fissionReactorPowerIterations", 10, 1, Integer.MAX_VALUE);
            heatExchangerEfficiencyMultiplier = builder
                    .comment("Nuclear coolant heat exchanger recipe efficiency multiplier for balancing purposes.",
                            "Default: 0.25")
                    .defineInRange("heatExchangerEfficiencyMultiplier", 0.25D, 0D, 1000D);
            enableMeltdown = builder
                    .comment("Whether to enable meltdowns and associated explosions or not.", "Default: true")
                    .define("enableMeltdown", true);
        }
    }

    public static final class MiscOptions {

        public final ForgeConfigSpec.BooleanValue enableMaterialModifications;
        public final ForgeConfigSpec.BooleanValue enableHX;
        public final ForgeConfigSpec.BooleanValue disableAllRecipes;
        public final ForgeConfigSpec.BooleanValue disableAllMaterials;
        public final ForgeConfigSpec.BooleanValue showFluidsForAutoFillingMultiblocks;
        public final ForgeConfigSpec.BooleanValue allowExtendedFacingForFissionReactor;
        public final ForgeConfigSpec.IntValue startIdShift;

        private MiscOptions(ForgeConfigSpec.Builder builder) {
            enableMaterialModifications = builder
                    .comment("Do material modifications, like adding flags or properties.",
                            "Modpack devs might want to disable this to reduce conflicts.",
                            "Default: true")
                    .define("enableMaterialModifications", true);
            enableHX = builder
                    .comment("Whether to register heat exchanger or not. Useful for SuSy.", "Default: true")
                    .define("enableHX", true);
            disableAllRecipes = builder
                    .comment("Remove all recipes from Supercritical.", "Default: false")
                    .define("disableAllRecipes", false);
            disableAllMaterials = builder
                    .comment("Remove all materials from Supercritical, except Corium.", "Default: false")
                    .define("disableAllMaterials", false);
            showFluidsForAutoFillingMultiblocks = builder
                    .comment("Make auto-filled fluid block show up in JEI / in-world preview.", "Default: false")
                    .define("showFluidsForAutoFillingMultiblocks", false);
            allowExtendedFacingForFissionReactor = builder
                    .comment("Allow extended facing for Fission Reactor.", "Default: false")
                    .define("allowExtendedFacingForFissionReactor", false);
            startIdShift = builder
                    .comment("The starting id offset retained as migration metadata for old Supercritical MTEs.",
                            "Use with CAUTION since this could void old MTEs in existing saves!", "Default: 0")
                    .defineInRange("startIdShift", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
    }
}
