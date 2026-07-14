
import net.neoforged.moddevgradle.dsl.RunModel
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    alias(libs.plugins.modDevGradle)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.ideaExt)
}

dependencies {
    if (useMixin) {
        annotationProcessor(variantOf(libs.mixin) { classifier("processor") })
        compileOnly(libs.mixinExtras.common)
        annotationProcessor(libs.mixinExtras.common)
        api(libs.mixinExtras.forge)
        jarJar(libs.mixinExtras.forge) {
            version { require("[${libs.versions.mixinExtras.get()},)") }
        }
    }
}

//configurations.configureEach {
//    resolutionStrategy.eachDependency {
//        if (requested.group == "org.lwjgl") {
//            useVersion(libs.versions.lwjgl.get())
//            because("LWJGL version > 3.3.1 is required for Java 21 RenderDoc support")
//        }
//    }
//}

// Co-locate processed resources with classes so Forge's dev-time mod locator finds META-INF/mods.toml.
sourceSets.main {
    output.setResourcesDir(java.classesDirectory)
    resources.srcDir("src/generated/resources")
}

legacyForge {

    version = "${libs.versions.minecraft.get()}-${libs.versions.forge.get()}"

    parchment {
        minecraftVersion = libs.versions.minecraft.get()
        mappingsVersion = libs.versions.parchment.get()
    }

    if (project.accessTransformers.isNotBlank()) {
        accessTransformers {
            from(project.accessTransformers.split(";").map { file("src/main/resources/META-INF/$it") })
        }
    }

    interfaceInjectionData.from(files("injected_interfaces/interfaces.json"))

    addModdingDependenciesTo(sourceSets.test.get())

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        create("client") {
            client()
            enableGameTest()
            ideName = "runClient"
            gameDirectory = file("run/client")
        }
        create("server") {
            server()
            enableGameTest()
            ideName = "runServer"
            gameDirectory = file("run/server")
        }
        create("data") {
            data()
            ideName = "DataGen"
            sourceSet = sourceSets.main.get()
            gameDirectory = file("run/data")
            programArguments.addAll(
                "--mod", modId,
                "--all",
                "--output", file("src/generated/resources").absolutePath,
                "--existing", file("src/main/resources/").absolutePath,
            )
        }

//        if (renderDocPath.isNotBlank() && !operatingSystem.startsWith("mac", ignoreCase = true)) {
//            create("clientWithRenderDoc") {
//                client()
//                ideName = "runClient with RenderDoc"
//                sourceSet = sourceSets.main.get()
//
//                programArguments.addAll("neoforge.rendernurse.renderdoc.library", renderDocPath)
//                if (OperatingSystem)
//            }
//        }

        configureEach {
            jvmArgument("-ea:$modGroup")
//            jvmArgument("-Dterminal.jline=true")
        }
    }
}

// Automatic constants generation with BuildConfig
if (generateTags) {
    buildConfig {
        packageName(modGroup)
        useKotlinOutput()
        buildConfigField("MOD_ID", modId)
        buildConfigField("MOD_NAME", modName)
        buildConfigField("net.minecraft.resources.ResourceLocation", "TEMPLATE_RL", "ResourceLocation.fromNamespaceAndPath(MOD_ID, \"\")")
    }
}

if (useMixin) {
    // Mixin refmap & config wiring
    mixin {
        config("${modId}.mixins.json")  // TODO)) scan & supply all mixin jsons?
        add(sourceSets.main.get(), mixinRefmap)
    }
}

tasks.processResources {
    if (!useMixin) exclude("*mixin*.json")

    val templateTokens = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to effectiveModVersion,
        "mc_version" to mcVersion,
        "mod_group" to modGroup,
        "mixin_package" to mixinPackage,
        "mixin_refmap" to mixinRefmap,
        "mixin_min_version" to libs.versions.mixin.get(),
        "mixinextras_min_version" to libs.versions.mixinExtras.get(),
    )

    // Template files for 1.20.1: mods.toml, pack.mcmeta, and mixin configs
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta", "*mixin*.json")) {
        filter<ReplaceTokens>("tokens" to templateTokens)
        expand(templateTokens)
    }
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            buildMap {
                put("Implementation-Title", modName)
                put("Implementation-Version", effectiveModVersion)
                if (useMixin) put("MixinConfigs", "$modId.mixins.json")
            }
        )
    }
}

private fun RunModel.enableGameTest() = systemProperty("forge.enabledGameTestNamespaces", modId)
