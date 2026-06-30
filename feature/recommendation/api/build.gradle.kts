plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.feature.recommendation.api"
}

dependencies {
    api(projects.core.content.api)
    api(projects.feature.feed.api)
    implementation(libs.kotlinx.coroutines.core)
}
