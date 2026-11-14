plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}
kotlin { jvmToolchain(21) }
application { mainClass.set("com.example.trainer.server.ApplicationKt") }
dependencies {
    val ktor = "3.4.3"
    implementation("io.ktor:ktor-server-netty:$ktor")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
    implementation("io.ktor:ktor-server-auth:$ktor")
    implementation("io.ktor:ktor-server-status-pages:$ktor")
    implementation("io.ktor:ktor-server-call-logging:$ktor")
    implementation("com.typesafe:config:1.4.3")
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.61.0")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.google.firebase:firebase-admin:9.7.0")
    implementation("io.insert-koin:koin-ktor:4.1.1")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    testImplementation("io.ktor:ktor-server-test-host:$ktor")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktor")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("com.h2database:h2:2.3.232")
}
tasks.register<JavaExec>("seed") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.example.trainer.server.db.SeedKt")
}



tasks.withType<Test>().configureEach { useJUnit() }
