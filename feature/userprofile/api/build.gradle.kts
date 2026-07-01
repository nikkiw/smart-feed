plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.feature.userprofile.api"
}

dependencies {
    api(projects.core.content.api)
    implementation(libs.kotlinx.coroutines.core)
}
