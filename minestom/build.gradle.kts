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
    implementation("org.readutf.buildformat:common:1.0.1")
    implementation("org.readutf.buildformat:s3:1.0.1")
    implementation("org.readutf.buildformat:postgres:1.0.1")

    implementation("org.tinylog:tinylog-api:2.7.0")
    implementation("org.tinylog:tinylog-impl:2.7.0")
    implementation("org.tinylog:slf4j-tinylog:2.7.0")
}

tasks.test {
    useJUnitPlatform()
}