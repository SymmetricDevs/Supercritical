@file:Suppress("UnstableApiUsage")

plugins {
    java
}

repositories {
    mavenLocal()
    mavenCentral()

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    exclusiveContent {
        forRepositories(
            maven {
                name = "Curse Maven"
                url = uri("https://www.cursemaven.com")
            },
            maven {
                name = "Curse Maven Mirror"
                url = uri("https://curse.cleanroommc.com")
            }
        )
        filter {
            includeGroup("curse.maven")
        }
    }

    exclusiveContent { // Create, Ponder, Flywheel
        forRepository {
            maven {
                name = "Create Maven"
                url = uri("https://maven.createmod.net")
            }
        }
        filter {
            includeGroup("net.createmod.ponder")
            includeGroup("com.simibubi.create")
            includeGroup("dev.engine-room.flywheel")
        }
    }

    exclusiveContent {
        forRepositories(
            maven {
                name = "Toma Maven"
                url = uri("https://repo.repsy.io/mvn/toma/public/")
            },
            maven {
                name = "Toma Maven Api"
                url = uri("https://api.repsy.io/mvn/toma/public")
            },
        )
        filter {
            includeGroup("dev.toma.configuration")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "KubeJS Maven"
                url = uri("https://maven.latvian.dev/releases")
            }
        }
        filter {
            includeGroup("dev.latvian.mods")
        }
    }

    exclusiveContent { // FTB mods
        forRepository {
            maven {
                name = "FTB Maven"
                url = uri("https://maven.ftb.dev/releases")
            }
        }
        filter {
            includeGroup("dev.ftb.mods")
        }
    }

    exclusiveContent { // JourneyMap API
        forRepository {
            maven {
                name = "JourneyMap Maven"
                url = uri("https://jm.gserv.me/repository/maven-public/")
            }
        }
        filter {
            includeGroup("info.journeymap")
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "EMI Maven"
                url = uri("https://maven.terraformersmc.com/releases/")
            }
        }
        filter {
            includeGroup("dev.emi")
        }
    }

    exclusiveContent { // shedaniel - REI, architectury, cloth-config
        forRepository {
            maven {
                name = "Architectury Maven"
                url = uri("https://maven.shedaniel.me/")
            }
        }
        filter {
            includeGroupAndSubgroups("me.shedaniel")
            includeGroup("dev.architectury")
        }
    }

    exclusiveContent { // tterrag - registrate
        forRepository {
            maven {
                name = "Tterrag Maven"
                url = uri("https://maven.tterrag.com/")
            }
        }
        filter {
            includeGroup("com.tterrag.registrate")
        }
    }

    exclusiveContent { // Curios
        forRepository {
            maven {
                name = "Curios Maven"
                url = uri("https://maven.theillusivec4.top/")
            }
        }
        filter {
            includeGroup("top.theillusivec4.curios")
        }
    }

    exclusiveContent { // KotlinForForge
        forRepository {
            maven {
                name = "KotlinForForge Maven"
                url = uri("https://thedarkcolour.github.io/KotlinForForge/")
            }
        }
        filter {
            includeGroup("thedarkcolour")
        }
    }

    exclusiveContent { // CC: Tweaked
        forRepository {
            maven {
                name = "CC:Tweaked Maven"
                url = uri("https://maven.squiddev.cc")
            }
        }
        filter {
            includeGroup("cc.tweaked")
        }
    }

    exclusiveContent { // Xaero's
        forRepository {
            maven {
                name = "Xaero's Maven"
                url = uri("https://chocolateminecraft.com/maven/")
            }
        }
        filter {
            includeGroupAndSubgroups("xaero")
        }
    }

    maven {
        name = "BlameJared Maven"
        url = uri("https://maven.blamejared.com")
    }

    maven {
        name = "GTNH Maven"
        url = uri("https://nexus.gtnewhorizons.com/repository/public/")
    }

    maven {
        name = "GTCEu Maven"
        url = uri("https://maven.gtceu.com")
    }

    maven {
        name = "ModMaven"
        url = uri("https://modmaven.dev")
    }

    maven {
        name = "FirstDarkDev Maven"
        url = uri("https://maven.firstdark.dev/snapshots")
    }

    maven {
        name = "NeoForged Maven"
        url = uri("https://maven.neoforged.net/releases")
    }

    maven {
        name = "LexForge Maven"
        url = uri("https://maven.minecraftforge.net")
    }
}
