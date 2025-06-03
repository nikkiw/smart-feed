plugins {
    id("ndev-android-library-with-hilt-convention")
}

android {
    namespace = "com.core.networks"

}


dependencies {
    implementation(projects.core.core)
    // Network - Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)
}