import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gradleup.shadow") version "9.6.1"
}

val javaFxVersion = "26.0.1"
val supportedJavaFxPlatforms = listOf("win", "mac", "linux")
val requiredJavaFxModules = listOf("base", "graphics", "controls")

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Package native JavaFX libraries for each supported desktop operating
    // system so the Shadow JAR does not depend on the build machine's OS.
    supportedJavaFxPlatforms.forEach { platform ->
        requiredJavaFxModules.forEach { module ->
            runtimeOnly("org.openjfx:javafx-$module:$javaFxVersion:$platform")
        }
    }
}

application {
    mainClass.set("fitlog.Launcher")
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = javaFxVersion
    modules("javafx.controls")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.jar {
    // Keep the thin application JAR distinct from the all-in-one Shadow JAR.
    archiveClassifier.set("plain")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("fitlog.jar")
}
