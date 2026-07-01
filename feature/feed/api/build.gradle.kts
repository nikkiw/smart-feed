plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.feature.feed.api"
}

dependencies {
    api(projects.core.content.api)

    implementation(libs.decompose)
    implementation(libs.androidx.pagging.ktx)
    implementation(libs.kotlinx.serialization.json)
}
