plugins {
    `java-library`
}

group = "org.readutf.game"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.panda-lang.org/releases") }
    maven { url = uri("https://mvn.utf.lol/releases") }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.readutf.arena:core:1.2.6")
    compileOnly("org.readutf.arena:minestom:1.2.6")

    api("net.minestom:minestom:2025.07.04-1.21.5")
    api("dev.rollczi:litecommands-bukkit:3.9.7")
    api("org.readutf.gameservice:client:1.0.14")
}

tasks.test {
    useJUnitPlatform()
}