plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.core.paging"
}

dependencies {
    implementation(projects.core.coreDomain)

    implementation(libs.androidx.pagging.ktx)
}
