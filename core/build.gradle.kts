import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}

tasks.named("build") {
    dependsOn("spotlessApply")
}
