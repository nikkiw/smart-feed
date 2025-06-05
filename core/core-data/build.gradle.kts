plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.core.data"

    packaging {
        resources {
            excludes += setOf("DebugProbesKt.bin", "META-INF/LICENSE", "META-INF/NOTICE")
            pickFirsts += "META-INF/*"
        }
    }
}


dependencies {
    implementation(projects.core.core)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreNetworks)
    implementation(projects.core.coreDatabase)

    // Pagging
    implementation(libs.room.paging)
    implementation(libs.androidx.pagging.ktx)

    implementation(libs.gson)

    // Testing dependencies
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.pagging.testing)
}