plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.core.data"

    defaultConfig {
        testInstrumentationRunner = "com.core.data.HiltCustomTestRunner"
    }

    packaging {
        resources {
            excludes += setOf("DebugProbesKt.bin", "META-INF/LICENSE", "META-INF/NOTICE")
            pickFirsts += "META-INF/*"
        }
    }
}

dependencies {
    implementation(projects.core.corePaging)

    // Testing dependencies
    // Unit tests
    testImplementation(libs.google.truth)
    testImplementation(libs.androidx.test.core.ktx)

    // Instrumental tests
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.pagging.testing)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.google.truth)
    androidTestImplementation(projects.core.image.api)
    androidTestImplementation(projects.feature.recommendation.local)
}
