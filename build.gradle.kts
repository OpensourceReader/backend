import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "6.20.0"
}

group = "com.opensourcereader"
version = "0.0.1-SNAPSHOT"
description = "OpenSourceReader"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
allprojects {
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenCentral()
    }

    spotless {
        lineEndings = com.diffplug.spotless.LineEnding.UNIX

        java {
            removeUnusedImports()
            googleJavaFormat()
            importOrder("java", "javax", "org.springframework", "", "lombok")
            indentWithSpaces()
            trimTrailingWhitespace()
        }

        kotlinGradle {
            target("*.gradle.kts")
            ktlint()

            indentWithSpaces()
            trimTrailingWhitespace()
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-web")
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testRuntimeOnly("com.h2database:h2")

        // auth
        implementation("com.auth0:java-jwt:4.4.0")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("build") {
    dependsOn("spotlessApply")
}
