plugins {
    `java-library`
    id("io.freefair.lombok") version "8.13.1"
}

group = "org.readutf.game"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
    maven {
        name = "utfRepoReleases"
        url = uri("https://mvn.utf.lol/releases")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    api("org.readutf.arena:core:1.2.3")
    api("org.readutf.arena:minestom:1.2.3")
    api(project(":common"))
    api("net.minestom:minestom-snapshots:1_21_5-69b9a5d844")

    api("io.github.togar2:minestompvp:1.0")
}

tasks.test {
    useJUnitPlatform()
}