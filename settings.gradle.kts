pluginManagement {
    operator fun Settings.get(property: String): String {
        return org.gradle.api.internal.plugins.DslObject(this).asDynamicObject.getProperty(property) as String
    }

    repositories {
        gradlePluginPortal()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven { url = uri("https://maven.architectury.dev/") }
        maven { url = uri("https://maven.minecraftforge.net/") }
        mavenLocal()
    }

    plugins {
        //id ("fabric-loom") version settings["loom_version"]
        id ("architectury-plugin") version settings["arch_plugin_version"]
        id ("dev.architectury.loom") version settings["loom_version"]
        id ("com.github.johnrengelman.shadow") version settings["shadow_version"]
        id ("io.github.juuxel.loom-quiltflower") version settings["quiltflower_version"]

        id ("org.ajoberstar.grgit") version settings["grgit_version"]
        id ("com.matthewprenger.cursegradle") version settings["cursegradle_version"]
        id ("com.modrinth.minotaur") version settings["modrinth_version"]
    }
}

include("common")
include("fabric")
include("forge")

rootProject.name = "builtin-servers"