import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    // auth
    implementation("org.springframework.security:spring-security-crypto")
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
