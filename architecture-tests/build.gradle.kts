plugins {
    alias(libs.plugins.kotlin.jvm)
    id("smart.feed.detekt")
    id("smart.feed.spotless")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.konsist)
}

tasks.withType<Test>().configureEach {
    useJUnit()
    systemProperty("smartFeed.rootDir", rootProject.projectDir.absolutePath)
}
