plugins {
    id("java")
    id("dev.architectury.loom") version("1.2-SNAPSHOT")
    id("architectury-plugin") version("3.4-SNAPSHOT")
    id("org.jetbrains.kotlin.jvm") version("1.8.21")
}


group = property("maven_group")!!
version = property("mod_version")!!

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    silentMojangMappingsLicense()

    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
}

repositories {
    mavenCentral()
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
}

dependencies {
    minecraft("net.minecraft:minecraft:${property("mc_version")}")
    mappings(loom.officialMojangMappings())
    forge("net.minecraftforge:forge:${property("forge_version")}")

//    modImplementation("com.cobblemon:forge:${property("cobblemon_version")}")
    modImplementation(files("libs/Cobblemon-forge-1.4.1+1.20.1-SNAPSHOT.jar"))
    implementation("thedarkcolour:kotlinforforge:${property("kff_version")}")
}