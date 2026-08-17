plugins {
    application
    java
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
}

application {
    mainClass.set("fitlog.FitLog")
}

tasks.test {
    useJUnitPlatform()
}
