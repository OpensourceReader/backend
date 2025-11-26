import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies{
    implementation(project(":core"))
    implementation("com.mysql:mysql-connector-j:8.0.33")
}

tasks.named<BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}