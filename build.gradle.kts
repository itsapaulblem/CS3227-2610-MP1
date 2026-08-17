import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gradleup.shadow") version "9.6.1"
}

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
}

application {
    mainClass.set("fitlog.Launcher")
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "26.0.1"
    modules("javafx.controls")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("fitlog.jar")
}
