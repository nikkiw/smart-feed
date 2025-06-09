plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.image.glide"
}


dependencies {
    implementation(projects.core.core)
    implementation(libs.bundles.glide)
    ksp(libs.glide.ksp)
}