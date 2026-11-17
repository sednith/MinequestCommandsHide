import java.io.File

plugins {
    java
}

group = "dev.sednith"
version = "1.0.0"

// Путь до plugins/ на Velocity-сервере для автокопирования собранного jar.
// Передаётся при сборке: -PvelocityPluginsDir=/path/to/velocity/plugins
val velocityPluginsDir: String? = providers.gradleProperty("velocityPluginsDir").orNull

val velocityVersion = "4.3.1"
val adventureVersion = "5.2.0"
val brigadierVersion = "1.0.0-SNAPSHOT"
val snakeYamlVersion = "2.7"
val slf4jVersion = "2.0.20"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:$velocityVersion")
    compileOnly("com.velocitypowered:velocity-brigadier:$brigadierVersion")
    compileOnly("net.kyori:adventure-api:$adventureVersion")
    compileOnly("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    compileOnly("org.yaml:snakeyaml:$snakeYamlVersion")
    compileOnly("org.slf4j:slf4j-api:$slf4jVersion")
    compileOnly("net.luckperms:api:5.5")

    annotationProcessor("com.velocitypowered:velocity-api:$velocityVersion")

    testImplementation("com.velocitypowered:velocity-api:$velocityVersion")
    testImplementation("com.velocitypowered:velocity-brigadier:$brigadierVersion")
    testImplementation("net.kyori:adventure-api:$adventureVersion")
    testImplementation("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    testImplementation("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    testImplementation("org.yaml:snakeyaml:$snakeYamlVersion")
    testImplementation("org.slf4j:slf4j-api:$slf4jVersion")
    testImplementation("net.luckperms:api:5.5")
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName = "MinequestCommandsHide"
    archiveVersion = project.version.toString()

    doLast {
        val builtJar = archiveFile.get().asFile
        val rootCopy = layout.projectDirectory.file(builtJar.name).asFile
        builtJar.copyTo(rootCopy, overwrite = true)

        if (!velocityPluginsDir.isNullOrBlank()) {
            val pluginsDir = File(velocityPluginsDir)
            pluginsDir.mkdirs()
            builtJar.copyTo(pluginsDir.resolve(builtJar.name), overwrite = true)
        }
    }
}
