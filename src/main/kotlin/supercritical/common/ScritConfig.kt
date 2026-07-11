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
 * `@Configurable @JvmField var` idiom on both group holders and leaves. Group holders are
 * pre-initialized so the library can read the existing instance and recurse into their fields.
 *
 * `init()` is invoked early in mod construction (see [supercritical.Supercritical]) so that
 * `INSTANCE` is populated before material/machine registration reads it. This replaces the old
 * ForgeConfigSpec approach which threw "Cannot get config value before config is loaded" when
 * values were read at registration time in the dev environment.
 */
@Config(id = BuildConfig.MOD_ID)
class ScritConfig {
    
    @Configurable
    @Configurable.Comment("Config options for Supercritical")
    @JvmField
    var nuclear = NuclearOptions()

    @Configurable
    @Configurable.Comment("Miscellaneous options for Supercritical")
    @JvmField
    var misc = MiscOptions()

    class NuclearOptions {
        @Configurable
        @Configurable.Comment("Nuclear Max Power multiplier for balancing purposes.", "Default: 0.1")
        @Configurable.DecimalRange(min = 0.0, max = 10000.0)
        @JvmField
        var nuclearPowerMultiplier = 0.1

        @Configurable
        @Configurable.Comment(
            "How much the amount of power required to boil a coolant is divided by.",
            "Default: 14"
        )
        @Configurable.DecimalRange(min = 0.1, max = 1000.0)
        @JvmField
        var fissionCoolantDivisor = 14.0

        @Configurable
        @Configurable.Comment(
            "The level of detail to which fission reactors are analyzed. May cause more lag at higher values.",
            "Default: 100"
        )
        @Configurable.DecimalRange(min = 5.0, max = 10000.0)
        @JvmField
        var fissionReactorResolution = 100.0

        @Configurable
        @Configurable.Comment(
            "The number of times the neutron multiplication value is calculated. May cause more lag at higher values.",
            "Default: 10"
        )
        @Configurable.Range(min = 1L, max = 2147483647L)
        @JvmField
        var fissionReactorPowerIterations = 10

        @Configurable
        @Configurable.Comment(
            "Nuclear coolant heat exchanger recipe efficiency multiplier for balancing purposes.",
            "Default: 0.25"
        )
        @Configurable.DecimalRange(min = 0.0, max = 1000.0)
        @JvmField
        var heatExchangerEfficiencyMultiplier = 0.25

        @Configurable
        @Configurable.Comment("Whether to enable meltdowns and associated explosions or not.", "Default: true")
        @JvmField
        var enableMeltdown = true
    }

    class MiscOptions {
        @Configurable
        @Configurable.Comment(
            "Do material modifications, like adding flags or properties.",
            "Modpack devs might want to disable this to reduce conflicts.",
            "Requires a game restart.",
            "Default: true"
        )
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        @JvmField
        var enableMaterialModifications = true

        @Configurable
        @Configurable.Comment("Remove all recipes from Supercritical.", "Default: false")
        @JvmField
        var disableAllRecipes = false

        @Configurable
        @Configurable.Comment(
            "Remove all materials from Supercritical, except Corium.",
            "Requires a game restart.",
            "Default: false"
        )
        @Configurable.UpdateRestriction(UpdateRestrictions.GAME_RESTART)
        @JvmField
        var disableAllMaterials = false

        @Configurable
        @Configurable.Comment("Make auto-filled fluid block show up in JEI / in-world preview.", "Default: false")
        @JvmField
        var showFluidsForAutoFillingMultiblocks = false

        @Configurable
        @Configurable.Comment("Allow extended facing for Fission Reactor.", "Default: false")
        @JvmField
        var allowExtendedFacingForFissionReactor = false
    }

    companion object {
        @JvmStatic
        lateinit var INSTANCE: ScritConfig
            private set

        @JvmStatic
        fun init() {
            INSTANCE = Configuration.registerConfig(ScritConfig::class.java, ConfigFormats.YAML)
                .getConfigInstance()
        }
    }
}
