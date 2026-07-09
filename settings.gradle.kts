rootProject.name = "Supercritical"

dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            from(files("./gradle/deps.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    // Automatic toolchain provisioning
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

includeBuild("build-logic")
