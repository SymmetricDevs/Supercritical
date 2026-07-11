package supercritical.common

import dev.toma.configuration.Configuration
import dev.toma.configuration.config.Config
import dev.toma.configuration.config.Configurable
import dev.toma.configuration.config.UpdateRestrictions
import dev.toma.configuration.config.format.ConfigFormats
import supercritical.BuildConfig

/**
 * Mirrors GTCEu's [com.gregtechceu.gtceu.config.ConfigHolder]: a plain class annotated with
 * `@Config` whose fields are resolved by the dev.toma.configuration library via reflection.
 *
 * The configuration library collects fields through `Class.getFields()` and rejects static or
 * final fields (it logs "only instance non-final types are supported"). In Kotlin this means
 * every `@Configurable` field must be a real, public, mutable backing field, hence the
 * `@field:Configurable @JvmField var` idiom on both group holders and leaves. Group holders are
 * pre-initialized so the library can read the existing instance and recurse into their fields.
 *
 * `init()` is invoked early in mod construction (see [supercritical.Supercritical]) so that
 * `INSTANCE` is populated before material/machine registration reads it. This replaces the old
 * ForgeConfigSpec approach which threw "Cannot get config value before config is loaded" when
 * values were read at registration time in the dev environment.
 */
@Config(id = BuildConfig.MOD_ID)
class SCConfigHolder {
    @field:Configurable
    @field:Configurable.Comment("Config options for Supercritical")
    @JvmField
    var nuclear = NuclearOptions()

    @field:Configurable
    @field:Configurable.Comment("Miscellaneous options for Supercritical")
    @JvmField
    var misc = MiscOptions()

    class NuclearOptions {
        @field:Configurable
        @field:Configurable.Comment("Nuclear Max Power multiplier for balancing purposes.", "Default: 0.1")
        @field:Configurable.DecimalRange(min = 0.0, max = 10000.0)
        @JvmField
        var nuclearPowerMultiplier = 0.1

        @field:Configurable
        @field:Configurable.Comment(
            "How much the amount of power required to boil a coolant is divided by.",
            "Default: 14"
        )
        @field:Configurable.DecimalRange(min = 0.1, max = 1000.0)
        @JvmField
        var fissionCoolantDivisor = 14.0

        @field:Configurable
        @field:Configurable.Comment(
            "The level of detail to which fission reactors are analyzed. May cause more lag at higher values.",
            "Default: 100"
        )
        @field:Configurable.DecimalRange(min = 5.0, max = 10000.0)
        @JvmField
        var fissionReactorResolution = 100.0

        @field:Configurable
        @field:Configurable.Comment(
            "The number of times the neutron multiplication value is calculated. May cause more lag at higher values.",
            "Default: 10"
        )
        @field:Configurable.Range(min = 1L, max = 2147483647L)
        @JvmField
        var fissionReactorPowerIterations = 10

        @field:Configurable
        @field:Configurable.Comment(
            "Nuclear coolant heat exchanger recipe efficiency multiplier for balancing purposes.",
            "Default: 0.25"
        )
        @field:Configurable.DecimalRange(min = 0.0, max = 1000.0)
        @JvmField
        var heatExchangerEfficiencyMultiplier = 0.25

        @field:Configurable
        @field:Configurable.Comment("Whether to enable meltdowns and associated explosions or not.", "Default: true")
        @JvmField
        var enableMeltdown = true
    }

    class MiscOptions {
        @field:Configurable
        @field:Configurable.Comment(
            "Do material modifications, like adding flags or properties.",
            "Modpack devs might want to disable this to reduce conflicts.",
            "Requires a game restart.",
            "Default: true"
        )
        @field:Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        @JvmField
        var enableMaterialModifications = true

        @field:Configurable
        @field:Configurable.Comment("Whether to register heat exchanger or not. Useful for SuSy.", "Default: true")
        @JvmField
        var enableHX = true

        @field:Configurable
        @field:Configurable.Comment("Remove all recipes from Supercritical.", "Default: false")
        @JvmField
        var disableAllRecipes = false

        @field:Configurable
        @field:Configurable.Comment(
            "Remove all materials from Supercritical, except Corium.",
            "Requires a game restart.",
            "Default: false"
        )
        @field:Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        @JvmField
        var disableAllMaterials = false

        @field:Configurable
        @field:Configurable.Comment("Make auto-filled fluid block show up in JEI / in-world preview.", "Default: false")
        @JvmField
        var showFluidsForAutoFillingMultiblocks = false

        @field:Configurable
        @field:Configurable.Comment("Allow extended facing for Fission Reactor.", "Default: false")
        @JvmField
        var allowExtendedFacingForFissionReactor = false

        @field:Configurable
        @field:Configurable.Comment(
            "The starting id offset retained as migration metadata for old Supercritical MTEs.",
            "Use with CAUTION since this could void old MTEs in existing saves!",
            "Default: 0"
        )
        @field:Configurable.Range(min = -2147483648L, max = 2147483647L)
        @JvmField
        var startIdShift = 0
    }

    companion object {
        // Populated once by init() during early mod construction, before any registration reads
        // it (see class doc). Non-null for the rest of the mod lifecycle, so `lateinit` removes the
        // need for `!!` at every call site. (The dev.toma.configuration library reflects on the
        // *instance* @Configurable fields above, not on this companion field, so non-null is safe.)
        @JvmStatic
        lateinit var INSTANCE: SCConfigHolder
            private set

        @JvmStatic
        fun init() {
            // ConfigFormats.{yaml,json,properties}() are @Deprecated(forRemoval=true) since 3.0;
            // the non-deprecated API is the equivalent public static field (ConfigFormats.YAML),
            // which is the exact same IConfigFormatHandler instance the method returned.
            INSTANCE = Configuration.registerConfig(SCConfigHolder::class.java, ConfigFormats.YAML)
                .getConfigInstance()
        }
    }
}
