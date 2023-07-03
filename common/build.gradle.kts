operator fun Project.get(property: String): String = property(property) as String

architectury {
    common(rootProject["enabled_platforms"].split(","))
}

loom {
    //accessWidenerPath.set(file("src/main/resources/builtinservers.accesswidener"))
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject["fabric_loader_version"]}")
}

publishing {
    publications {
        register("mavenCommon", MavenPublication::class){
            artifactId = base.archivesName.get();
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}
