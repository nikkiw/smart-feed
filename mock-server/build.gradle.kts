plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}


group = "com.example"
version = "0.1.0"

// The main starter class
application {
    // specify the path to the main function
    mainClass.set("com.example.server.ApplicationKt")
}

// Set Java compilation to run with Compatibility = 17
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


dependencies {
    // Ktor core + Netty
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    // Content negotiation + kotlinx.serialization
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.serialization.kotlinx.json.jvm)
    // Call Logging
    implementation(libs.ktor.server.call.logging.jvm)
    // SLF4J backend (Logback)
    implementation(libs.logback.classic)

    // For tests
    testImplementation(libs.ktor.server.tests.jvm)
    testImplementation(libs.kotlin.test.junit)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

// Optional: “alias” the shuffle to run via the root shuffle runMockServer
tasks.register("runMockServer") {
    dependsOn("run") // the run task is defined by the application plugin
    group = "mock"
    description = "Start a local Ktor server (mock-backend) to debug the Android application"
}
