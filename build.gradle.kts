import org.gradle.api.Project.DEFAULT_VERSION
import org.springframework.boot.gradle.tasks.bundling.BootJar

/** --- configuration functions --- */
fun getGitHash(): String {
    return runCatching {
        providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }.standardOutput.asText.get().trim()
    }.getOrElse { "init" }
}

/** --- project configurations --- */
plugins {
    java
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    id("com.diffplug.spotless") version "6.20.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

allprojects {
    val projectGroup: String by project
    group = projectGroup
    version = if (version == DEFAULT_VERSION) getGitHash() else version

    apply(plugin = "com.diffplug.spotless")

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
    repositories {
        mavenCentral()
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
        testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.1.15")
    }

    tasks.withType(Jar::class) { enabled = true }
    tasks.withType(BootJar::class) { enabled = false }

    configure(allprojects.filter { it.path == ":core:core-api" }) {
        tasks.withType<Jar> {
            enabled = false
        }
        tasks.withType<BootJar> {
            enabled = true
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.named("build") {
    dependsOn("spotlessApply")
}

// module-container 는 task 를 실행하지 않도록 한다.
project("core") { tasks.configureEach { enabled = false } }
