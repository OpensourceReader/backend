import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    // auth
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
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
