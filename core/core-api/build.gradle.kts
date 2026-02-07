import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation(project(":core:core-domain"))
    implementation("org.apache.httpcomponents.client5:httpclient5")

    // DB
    implementation("com.mysql:mysql-connector-j:8.0.33")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // auth
    implementation("org.springframework.boot:spring-boot-starter-security")

    // testcontainer
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")

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
