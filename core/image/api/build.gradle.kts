plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.core.image.api"
}

dependencies {
    implementation(libs.hilt.android.core)
}
