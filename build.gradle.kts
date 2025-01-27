import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI
import java.util.*

fun loadProperties(filename: String): Properties {
    val properties = Properties()
    if (!file(filename).exists()) {
        return properties
    }
    file(filename).inputStream().use { properties.load(it) }
    return properties
}

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.3"
    id("maven-publish")
}

group = "gg.auroramc"
version = "1.5.1"

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21


repositories {
    mavenCentral()
    mavenLocal()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.auroramc.gg/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://repo.oraxen.com/snapshots")
    maven("https://repo.nexomc.com/snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("gg.auroramc:Aurora:2.1.2")
    compileOnly("gg.auroramc:AuroraLevels:1.6.2")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("dev.aurelium:auraskills-api-bukkit:2.0.7")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("com.github.Xiao-MoMi:Custom-Fishing:2.3.3")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10-SNAPSHOT")
    compileOnly("io.th0rgal:oraxen:2.0-SNAPSHOT")
    compileOnly("com.sarry20:TopMinion:2.4.3")
    compileOnly("org.me.leo_s:BeeMinions:3.0.6-BETA")
    compileOnly("com.nexomc:nexo:0.1.0-dev.0")

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}


tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    archiveFileName.set("AuroraCollections-${project.version}.jar")

    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    relocate("co.aikar.commands", "gg.auroramc.collections.libs.acf")
    relocate("co.aikar.locales", "gg.auroramc.collections.libs.locales")
    relocate("org.bstats", "gg.auroramc.collections.libs.bstats")

    exclude("acf-*.properties")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

val publishing = loadProperties("publish.properties")

publishing {
    repositories {
        maven {
            name = "AuroraMC"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                URI.create("https://repo.auroramc.gg/repository/maven-snapshots/")
            } else {
                URI.create("https://repo.auroramc.gg/repository/maven-releases/")
            }
            credentials {
                username = publishing.getProperty("username")
                password = publishing.getProperty("password")
            }
        }
    }

    publications.create<MavenPublication>("mavenJava") {
        groupId = "gg.auroramc"
        artifactId = "AuroraCollections"
        version = project.version.toString()

        from(components["java"])
    }
}