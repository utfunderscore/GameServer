plugins {
    id("java")
    application
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

    implementation(project(":tnttag"))
    implementation(project(":common"))
    implementation("net.minestom:minestom-snapshots:1_21_5-69b9a5d844")
}

tasks.test {
    useJUnitPlatform()
}