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
    modCompileOnlyApi(deps.gtceu) { isTransitive = false }
    compileOnly(deps.bundles.ccTweaked.api)

    modRuntimeOnly(deps.ldlib) // Forces a newer version of ldlib that contains SliderWidget
    modRuntimeOnly(deps.bundles.jei)
    modRuntimeOnly(deps.bundles.jade)
    modRuntimeOnly(deps.gtceu)
    modRuntimeOnly(deps.ccTweaked.forge)

//    val gtceuJar = configurations.detachedConfiguration(deps.gtceu.get()).singleFile
//    val jarJarDir = layout.buildDirectory.dir("gtceu-jarjar")
//    val extractGtceuJarJars = tasks.register<Copy>("extractGtceuJarJars") {
//        from(zipTree(gtceuJar)) {
//            include("META-INF/jarjar/Registrate-MC1.20-1.3.11.jar")
//            include("META-INF/jarjar/ldlib-forge-1.20.1-1.0.40.b.jar")
//            eachFile { path = name }
//            includeEmptyDirs = false
//        }
//        into(jarJarDir)
//    }
//    tasks.compileJava { dependsOn(extractGtceuJarJars) }
//    compileOnlyApi(files(
//        jarJarDir.map { it.file("Registrate-MC1.20-1.3.11.jar") },
//        jarJarDir.map { it.file("ldlib-forge-1.20.1-1.0.40.b.jar") },
//    ))

}