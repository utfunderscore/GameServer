plugins {
    `java-library`
}

group = "org.readutf.game"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://mvn.utf.lol/releases") }
    maven { url = uri("https://repo.panda-lang.org/releases") }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.readutf.arena:core:1.2.3")
    implementation("org.readutf.arena:minestom:1.2.3")
    implementation(project(":core"))
    implementation("net.minestom:minestom:2025.07.04-1.21.5")

    implementation("io.github.togar2:minestompvp:1.0")
}

tasks.test {
    useJUnitPlatform()
}