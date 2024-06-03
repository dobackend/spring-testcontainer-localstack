plugins {
    `java-library`
    `maven-publish`

}

group = "net.roodt"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.0")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("myLibrary") {
            from(components["java"])
        }
    }
//
//    repositories {
//        maven {
//            name = "localMaven"
//            url = uri("~/.m2/repository")
//        }
//    }
}