plugins {
    alias(libs.plugins.kotlin.jvm)
    id("smart.feed.detekt")
    id("smart.feed.spotless")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
