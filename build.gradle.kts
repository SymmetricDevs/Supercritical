@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

plugins {
    alias(conventions.plugins.repositories)
    alias(conventions.plugins.minecraft)
    alias(conventions.plugins.publish)
    alias(conventions.plugins.shadow)
    alias(conventions.plugins.jvmdg)
    alias(conventions.plugins.idea)
    alias(conventions.plugins.test)
    alias(conventions.plugins.jvm)
    alias(deps.plugins.lombok)
}

dependencies {
    fun Provider<MinimalExternalModuleDependency>.deobf() = get().let {
        rfg.deobf("${it.module.group}:${it.module.name}:${it.versionConstraint.requiredVersion}")
    }

    api(deps.gregtech)
    api(deps.openComputers.deobf())
    api(deps.sussyPatches.deobf())

    compileOnlyApi(deps.jspecify)
    compileOnlyApi(deps.annotations)

    // Mixinbooter 11.x breaks runtime (mixins with type-parameters, FMLDeobfuscatingRemapper)
    // So we use Mixinbooter 10.x here, which contains the mixin annotation processor.
    annotationProcessor(libs.mixinbooter)

    compileOnly(deps.gregicalityMultiblocks.deobf())
    compileOnly(deps.gregtechFoodOption.deobf())
    compileOnly(deps.susyCore.deobf())
    compileOnly(deps.geckolib.deobf())
    compileOnly(deps.universalModCore.deobf())
    compileOnly(deps.trackApi.deobf())
    compileOnly(deps.immersiveRailroading.deobf())
    compileOnly(deps.pyrotech.deobf())
    compileOnly(deps.athenaeum.deobf())
    compileOnly(deps.dropt.deobf())
    compileOnly(deps.hei)

    testImplementation(deps.assertj.core)

    runtimeOnly(deps.hei)
    runtimeOnly(deps.theOneProbe)
    runtimeOnly(deps.ctm) { isTransitive = false }
}

configurations {
    compileOnly {
        // exclude GNU trove, FastUtil is superior and still updated
        exclude(group = "net.sf.trove4j", module = "trove4j")
        // exclude javax.annotation from findbugs, JetBrains annotations are superior
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        // exclude scala as we don't use it for anything and causes import confusion
        exclude(group = "org.scala-lang")
        exclude(group = "org.scala-lang.modules")
        exclude(group = "org.scala-lang.plugins")
    }
}
