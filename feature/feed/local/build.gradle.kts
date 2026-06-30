plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.feature.feed.local"
}

dependencies {
    implementation(projects.feature.feed.api)

    implementation(libs.room.runtime)
    implementation(libs.room.common)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.androidx.pagging.ktx)
    implementation(libs.gson)
}
