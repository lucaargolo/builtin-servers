operator fun Project.get(property: String): String = property(property) as String

plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

//Not to sure this is correct
val common by configurations.creating
val shadowCommon by configurations.creating

configurations {
    common
    shadowCommon // Don't use shadow from the shadow plugin since it *excludes* files.
    compileClasspath { extendsFrom(common) }
    runtimeClasspath { extendsFrom(common) }
    "developmentFabric" { extendsFrom(common) }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject["fabric_loader_version"]}")
    modApi("net.fabricmc.fabric-api:fabric-api:${rootProject["fabric_api_version"]}")

    common(project(":common", "namedElements")) { isTransitive=false }
    shadowCommon(project(":common", "transformProductionFabric")) { isTransitive=false }
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}


tasks.shadowJar {
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveClassifier.set("fabric-dev-shadow")
}

tasks.remapJar {
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    dependsOn.add("shadowJar")
    archiveClassifier.set("fabric")
}

tasks.jar {
    archiveClassifier.set("fabric-dev")
}

tasks.sourcesJar {
    val commonSources = project(":common").tasks.sourcesJar
    dependsOn(commonSources)
    from(commonSources.get().archiveFile.map { zipTree(it) })
    archiveClassifier.set("fabric-sources")
}

with(components["java"] as AdhocComponentWithVariants) {
    withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) { skip() }
}

publishing {
    publications {
        register("mavenFabric", MavenPublication::class){
            artifactId = "${base.archivesName.get()}-${project.name}"
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}

//---

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    inputs.property("version", project.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
        expand(mutableMapOf("version" to project.version))
    }

    from(sourceSets["main"].resources.srcDirs) {
        exclude("fabric.mod.json")
    }
}
