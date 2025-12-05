import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation(project(":core"))
    implementation("com.mysql:mysql-connector-j:8.0.33")

    // auth
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
}

tasks.named<BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("build") {
    dependsOn("spotlessApply")
}
