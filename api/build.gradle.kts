import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation(project(":core"))

    // DB
    implementation("com.mysql:mysql-connector-j:8.0.33")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // auth
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // testcontainer
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
}

tasks.named<BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}
