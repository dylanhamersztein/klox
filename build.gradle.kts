plugins {
    kotlin("jvm") version "2.1.10"
}

group = "org.hamersztein"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
}
kotlin {
    jvmToolchain(21)
}