@file:Suppress("AvoidDuplicateDependencies")

plugins {
    alias(conventions.plugins.repositories)
    alias(conventions.plugins.minecraft)
    alias(conventions.plugins.shadow)
    alias(conventions.plugins.idea)
    alias(conventions.plugins.test)
    alias(conventions.plugins.jvm)
}

repositories {
    exclusiveContent { // ScalaLanguageProvider
        forRepository {
            maven {
                name = "Azure-SLP"
                url = uri("https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1")
            }
        }
        filter {
            includeModule("com.kotori316", "scalablecatsforce")
            includeModule("org.typelevel", "cats-core_3")
            includeModule("org.typelevel", "cats-kernel_3")
            includeModule("org.typelevel", "cats-free_3")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "OC:CE Maven"
                url = uri("https://maven.akkiserver.uk/releases/")
            }
        }
        filter {
            includeModule("li.cil.oc", "opencomputers")
        }
    }
}

dependencies {
    compileOnlyApi(deps.jspecify)
    compileOnlyApi(deps.annotations)

    modCompileOnlyApi(deps.bundles.jei)
    modCompileOnlyApi(deps.bundles.rei)
    modCompileOnlyApi(deps.emi)
    modCompileOnlyApi(deps.ldlib)
    modCompileOnlyApi(deps.registrate)
    modCompileOnlyApi(deps.configuration)
    modCompileOnlyApi(variantOf(deps.gtceu) { classifier("slim") }) { isTransitive = false }
    modCompileOnlyApi(deps.openComputersCE)
    modCompileOnlyApi(deps.bundles.ccTweaked.api)

    modRuntimeOnly(deps.configuration) // Forces a newer version of ldlib that contains ConfigFormats#YAML
    modRuntimeOnly(deps.ldlib) // Forces a newer version of ldlib that contains SliderWidget
    modRuntimeOnly(deps.jei.forge.impl)
//    modRuntimeOnly(deps.bundles.rei.runtime)
//    modRuntimeOnly(deps.emi)
    modRuntimeOnly(deps.bundles.jade)
    modRuntimeOnly(deps.spark)
    modRuntimeOnly(deps.gtceu)
    modRuntimeOnly(deps.ccTweaked.forge.impl)
    modRuntimeOnly(variantOf(deps.scalableCatsForce) { classifier("with-library") }) { isTransitive = false }
    modRuntimeOnly(deps.openComputersCE)
}