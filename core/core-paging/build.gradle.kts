plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.core.paging"
}

dependencies {
    api(projects.feature.feed.api)

    implementation(libs.androidx.pagging.ktx)
}
