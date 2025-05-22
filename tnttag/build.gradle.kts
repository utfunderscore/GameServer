plugins {
    id("java")
}

group = "org.readutf.game"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "utfRepoReleases"
        url = uri("https://mvn.utf.lol/releases")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.readutf.arena:core:1.2.2")
    implementation("org.readutf.arena:minestom:1.2.2")
    implementation(project(":common"))
    compileOnly("net.minestom:minestom-snapshots:1_21_5-69b9a5d844")
}

tasks.test {
    useJUnitPlatform()
}