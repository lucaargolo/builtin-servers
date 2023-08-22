import org.ajoberstar.grgit.Grgit
import org.kohsuke.github.GitHub
import org.kohsuke.github.GHReleaseBuilder
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import java.util.*

operator fun Project.get(property: String): String = property(property) as String

buildscript {
    dependencies {
        classpath("org.kohsuke:github-api:${project.property("github_api_version") as String}")
    }
}

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom") apply false
    id("io.github.juuxel.loom-quiltflower") apply false

    id("base") //Gradle Base Plugin

    id("org.ajoberstar.grgit")
    id("com.matthewprenger.cursegradle")
    id("com.modrinth.minotaur")
}

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    mavenLocal()
}

architectury {
    minecraft = rootProject["minecraft_version"]
}

allprojects {
    apply {
        plugin("java")
        plugin("architectury-plugin")
        plugin("base")

        plugin("maven-publish")
    }

    base {
        archivesName.set(rootProject["archives_base_name"])
        version = rootProject["mod_version"]
        group = rootProject["maven_group"]
    }

    repositories {}

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<Jar> {
        from("LICENSE")
    }
}

subprojects {
    apply {
        plugin("dev.architectury.loom")
        plugin("io.github.juuxel.loom-quiltflower")

        plugin("maven-publish")
        plugin("org.ajoberstar.grgit")
        plugin("com.matthewprenger.cursegradle")
        plugin("com.modrinth.minotaur")
    }

    //Due to Kotlin's handling of Accessors, it seems that we don't get the helper method ):
    extensions.getByName<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom").apply {
        silentMojangMappingsLicense()
    }

    dependencies {
        "minecraft" ("com.mojang:minecraft:${rootProject["minecraft_version"]}")
        "mappings" ("net.fabricmc:yarn:${rootProject["yarn_mappings"]}:v2")
    }

    fun String.capitalizeFirstChar(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val environment: Map<String, String> = System.getenv()
    val releaseName = "${rootProject.name.split("-").joinToString(" ") { it.capitalizeFirstChar() }} ${(version as String).split("+")[0]}"
    val releaseType = (version as String).split("+")[0].split("-").let { if(it.size > 1) if(it[1] == "BETA" || it[1] == "ALPHA") it[1] else "ALPHA" else "RELEASE" }
    val releaseFile = "${buildDir}/libs/${base.archivesName.get()}-${version}-${project.name}.jar"
    val cfGameVersion = (version as String).split("+")[1].let{ if(!rootProject["minecraft_version"].contains("-") && rootProject["minecraft_version"].startsWith(it)) rootProject["minecraft_version"] else "$it-Snapshot"}

    fun getChangeLog(): String = "A changelog can be found at https://github.com/lucaargolo/${rootProject.name}/commits/"

    fun getBranch(): String {
        environment["GITHUB_REF"]?.let { branch -> branch.substring(branch.lastIndexOf("/") + 1) }
        val grgit = try {
            extensions.getByName("grgit") as Grgit
        } catch (ignored: Exception) {
            return "unknown"
        }
        val branch = grgit.branch.current().name
        return branch.substring(branch.lastIndexOf("/") + 1)
    }

    apply {
        if(project.name == "common") return@apply

        //GitHub publishing
        task("github") {
            dependsOn("remapJar")
            group = "upload"

            onlyIf { environment.containsKey("GITHUB_TOKEN") }

            doLast {
                val gh = GitHub.connectUsingOAuth(environment["GITHUB_TOKEN"])
                val ghRepo = gh.getRepository(environment["GITHUB_REPOSITORY"])
                val ghTag = version.toString()

                var ghRelease = ghRepo.getReleaseByTagName(ghTag)
                if(ghRelease == null) {

                    val releaseBuilder = GHReleaseBuilder(ghRepo, ghTag)
                    releaseBuilder.name(releaseName)
                    releaseBuilder.body(getChangeLog())
                    releaseBuilder.commitish(getBranch())

                    ghRelease = releaseBuilder.create()
                }
                ghRelease.uploadAsset(file(releaseFile), "application/java-archive")
            }
        }

        //Curseforge publishing
        curseforge {
            environment["CURSEFORGE_API_KEY"]?.let { apiKey = it }

            project(closureOf<CurseProject> {
                id = project["curseforge_id"]
                changelog = getChangeLog()
                this.releaseType = releaseType.lowercase(Locale.getDefault())
                addGameVersion(cfGameVersion)
                addGameVersion(project.name.capitalizeFirstChar())

                mainArtifact(file(releaseFile), closureOf<CurseArtifact> {
                    displayName = releaseName
                    if(project.name.contains("fabric")) {
                        relations(closureOf<CurseRelation> {
                            requiredDependency("fabric-api")
                        })
                    }
                })

                afterEvaluate {
                    uploadTask.dependsOn("remapJar")
                }

            })

            options(closureOf<Options> {
                forgeGradleIntegration = false
            })
        }

        //Modrinth publishing
        modrinth {
            environment["MODRINTH_TOKEN"]?.let { token.set(it) }

            projectId.set(project["modrinth_id"])
            changelog.set(getChangeLog())

            versionNumber.set(version as String)
            versionName.set(releaseName)
            versionType.set(releaseType.lowercase(Locale.getDefault()))

            uploadFile.set(project.tasks["transformProduction${project.name.capitalizeFirstChar()}"])

            gameVersions.add(project["minecraft_version"])
            loaders.add(project.name)

            if(project.name.contains("fabric")) {
                dependencies {
                    required.project("fabric-api")
                }
            }
        }
        tasks.modrinth.configure {
            group = "upload"
        }
    }
}


