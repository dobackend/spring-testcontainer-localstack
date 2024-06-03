plugins {
    id("java")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application {
    mainClass.set("com.roodt.Handler")
}

group = "com.roodt"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation ("software.amazon.awssdk:sns:2.21.20")
    implementation("software.amazon.awssdk:lambda:2.21.20") // If you need AWS Lambda client
    implementation ("org.slf4j:slf4j-nop:2.0.6")
    implementation ("com.amazonaws:aws-lambda-java-core:1.2.2")
    implementation ("com.amazonaws:aws-lambda-java-events:3.11.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}

tasks.test {
    useJUnitPlatform()
}

