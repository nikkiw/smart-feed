plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.feature.userprofile.local"
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.common)
    implementation(libs.room.ktx)
}
