plugins {
    alias(conventions.plugins.repositories)
    alias(conventions.plugins.minecraft)
    alias(conventions.plugins.shadow)
    alias(conventions.plugins.idea)
    alias(conventions.plugins.test)
    alias(conventions.plugins.jvm)
}

dependencies {
    compileOnlyApi(deps.jspecify)
    compileOnlyApi(deps.annotations)
    testImplementation(deps.assertj.core)

    modImplementation(deps.gtceu)
    // JEI for dev testing
    compileOnlyApi(deps.bundles.jei)
    modRuntimeOnly(deps.bundles.jei)

    val gtceuJar = configurations.detachedConfiguration(deps.gtceu.get()).singleFile
    val jarJarDir = layout.buildDirectory.dir("gtceu-jarjar")
    val extractGtceuJarJars = tasks.register<Copy>("extractGtceuJarJars") {
        from(zipTree(gtceuJar)) {
            include("META-INF/jarjar/Registrate-MC1.20-1.3.11.jar")
            include("META-INF/jarjar/ldlib-forge-1.20.1-1.0.40.b.jar")
            eachFile { path = name }
            includeEmptyDirs = false
        }
        into(jarJarDir)
    }
    tasks.compileJava { dependsOn(extractGtceuJarJars) }
    compileOnlyApi(files(
        jarJarDir.map { it.file("Registrate-MC1.20-1.3.11.jar") },
        jarJarDir.map { it.file("ldlib-forge-1.20.1-1.0.40.b.jar") },
    ))

    // Mixin annotation processor for compile-time @Shadow/@Inject support
    if (useMixin) {
        annotationProcessor(variantOf(libs.mixin) { classifier("processor") })
    }

}

configurations {
    compileOnly {
        // exclude GNU trove, FastUtil is superior and still updated
        exclude(group = "net.sf.trove4j", module = "trove4j")
        // exclude javax.annotation from findbugs, JetBrains annotations are superior
//        exclude(group = "com.google.code.findbugs", module = "jsr305")
        // exclude scala as we don't use it for anything and causes import confusion
        exclude(group = "org.scala-lang")
        exclude(group = "org.scala-lang.modules")
        exclude(group = "org.scala-lang.plugins")
    }
}
