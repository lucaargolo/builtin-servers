operator fun Project.get(property: String): String = property(property) as String

plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    //accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    forge {
        //convertAccessWideners.set(true)
        //extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)

        mixinConfig(
            "builtinservers-common.mixins.json",
            "builtinservers.mixins.json"
        )
    }
}

//Not to sure this is correct
val common by configurations.creating
val shadowCommon by configurations.creating

configurations {
    common
    shadowCommon // Don't use shadow from the shadow plugin since it *excludes* files.
    compileClasspath { extendsFrom(common) }
    runtimeClasspath { extendsFrom(common) }
    "developmentForge" { extendsFrom(common) }
}

dependencies {
    forge("net.minecraftforge:forge:${rootProject["forge_version"]}")

    common(project(":common", "namedElements")) { isTransitive=false }
    shadowCommon(project(":common", "transformProductionForge")) { isTransitive=false }
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/mods.toml") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    exclude("fabric.mod.json")
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveClassifier.set("forge-dev-shadow")
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
    dependsOn.add("shadowJar")
    archiveClassifier.set("forge")
}

tasks.jar {
    archiveClassifier.set("forge-dev")
}

tasks.sourcesJar {
    val commonSources = project(":common").tasks.sourcesJar
    dependsOn(commonSources)
    from(commonSources.get().archiveFile.map { zipTree(it) } )
    archiveClassifier.set("forge-sources")
}

with(components["java"] as AdhocComponentWithVariants) {
    withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) { skip() }
}

publishing {
    publications {
        register("mavenForge", MavenPublication::class){
            artifactId = "${base.archivesName.get()}-${project.name}"
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}
