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
    maven {
        name = "CC:Tweaked Maven"
        url = uri("https://maven.squiddev.cc")
    }
}

dependencies {
    modCompileOnlyApi(deps.bundles.jei)
    modCompileOnlyApi(deps.ldlib)
    modCompileOnlyApi(deps.registrate)
    modCompileOnlyApi(deps.configuration)
    modCompileOnlyApi(variantOf(deps.gtceu) { classifier("slim") }) { isTransitive = false }
    compileOnly(deps.bundles.ccTweaked.api)

    modRuntimeOnly(deps.configuration) // Forces a newer version of ldlib that contains ConfigFormats#YAML
    modRuntimeOnly(deps.ldlib) // Forces a newer version of ldlib that contains SliderWidget
    modRuntimeOnly(deps.bundles.jei)
    modRuntimeOnly(deps.bundles.jade)
    modRuntimeOnly(deps.gtceu)
    modRuntimeOnly(deps.ccTweaked.forge)
}