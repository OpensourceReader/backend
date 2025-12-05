import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    //auth
    implementation("com.auth0:java-jwt:4.4.0")
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
