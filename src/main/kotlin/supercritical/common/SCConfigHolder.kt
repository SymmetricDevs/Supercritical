package supercritical.common

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig

object SCConfigHolder {
    val COMMON_SPEC: ForgeConfigSpec?
    val NUCLEAR: NuclearOptions
    val MISC: MiscOptions

    init {
        val builder = ForgeConfigSpec.Builder()
        builder.comment("Config options for Supercritical").push("nuclear")
        NUCLEAR = NuclearOptions(builder)
        builder.pop()

        builder.comment("Miscellaneous options for Supercritical").push("misc")
        MISC = MiscOptions(builder)
        builder.pop()

        COMMON_SPEC = builder.build()
    }

    fun register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC)
    }

    class NuclearOptions private constructor(builder: ForgeConfigSpec.Builder) {
        val nuclearPowerMultiplier: ForgeConfigSpec.DoubleValue?
        val fissionCoolantDivisor: ForgeConfigSpec.DoubleValue?
        val fissionReactorResolution: ForgeConfigSpec.DoubleValue?
        val fissionReactorPowerIterations: ForgeConfigSpec.IntValue?
        val heatExchangerEfficiencyMultiplier: ForgeConfigSpec.DoubleValue?
        val enableMeltdown: ForgeConfigSpec.BooleanValue?

        init {
            nuclearPowerMultiplier = builder
                .comment("Nuclear Max Power multiplier for balancing purposes.", "Default: 0.1")
                .defineInRange("nuclearPowerMultiplier", 0.1, 0.0, 10000.0)
            fissionCoolantDivisor = builder
                .comment("How much the amount of power required to boil a coolant is divided by.", "Default: 14")
                .defineInRange("fissionCoolantDivisor", 14.0, 0.1, 1000.0)
            fissionReactorResolution = builder
                .comment(
                    "The level of detail to which fission reactors are analyzed. May cause more lag at higher values.",
                    "Default: 100"
                )
                .defineInRange("fissionReactorResolution", 100.0, 5.0, 10000.0)
            fissionReactorPowerIterations = builder
                .comment(
                    "The number of times the neutron multiplication value is calculated. May cause more lag at higher values.",
                    "Default: 10"
                )
                .defineInRange("fissionReactorPowerIterations", 10, 1, Int.MAX_VALUE)
            heatExchangerEfficiencyMultiplier = builder
                .comment(
                    "Nuclear coolant heat exchanger recipe efficiency multiplier for balancing purposes.",
                    "Default: 0.25"
                )
                .defineInRange("heatExchangerEfficiencyMultiplier", 0.25, 0.0, 1000.0)
            enableMeltdown = builder
                .comment("Whether to enable meltdowns and associated explosions or not.", "Default: true")
                .define("enableMeltdown", true)
        }
    }

    class MiscOptions private constructor(builder: ForgeConfigSpec.Builder) {
        val enableMaterialModifications: ForgeConfigSpec.BooleanValue?
        val enableHX: ForgeConfigSpec.BooleanValue?
        val disableAllRecipes: ForgeConfigSpec.BooleanValue?
        val disableAllMaterials: ForgeConfigSpec.BooleanValue?
        val showFluidsForAutoFillingMultiblocks: ForgeConfigSpec.BooleanValue?
        val allowExtendedFacingForFissionReactor: ForgeConfigSpec.BooleanValue?
        val startIdShift: ForgeConfigSpec.IntValue?

        init {
            enableMaterialModifications = builder
                .comment(
                    "Do material modifications, like adding flags or properties.",
                    "Modpack devs might want to disable this to reduce conflicts.",
                    "Default: true"
                )
                .define("enableMaterialModifications", true)
            enableHX = builder
                .comment("Whether to register heat exchanger or not. Useful for SuSy.", "Default: true")
                .define("enableHX", true)
            disableAllRecipes = builder
                .comment("Remove all recipes from Supercritical.", "Default: false")
                .define("disableAllRecipes", false)
            disableAllMaterials = builder
                .comment("Remove all materials from Supercritical, except Corium.", "Default: false")
                .define("disableAllMaterials", false)
            showFluidsForAutoFillingMultiblocks = builder
                .comment("Make auto-filled fluid block show up in JEI / in-world preview.", "Default: false")
                .define("showFluidsForAutoFillingMultiblocks", false)
            allowExtendedFacingForFissionReactor = builder
                .comment("Allow extended facing for Fission Reactor.", "Default: false")
                .define("allowExtendedFacingForFissionReactor", false)
            startIdShift = builder
                .comment(
                    "The starting id offset retained as migration metadata for old Supercritical MTEs.",
                    "Use with CAUTION since this could void old MTEs in existing saves!", "Default: 0"
                )
                .defineInRange("startIdShift", 0, Int.MIN_VALUE, Int.MAX_VALUE)
        }
    }
}
