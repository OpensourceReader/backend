import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies{
    implementation(project(":core"))
}

tasks.named<BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}