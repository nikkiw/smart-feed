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
    implementation(projects.core.core)
    implementation(projects.core.coreDomain)
    implementation(projects.core.corePaging)
    implementation(projects.core.coreNetworks)
    implementation(projects.core.coreDatabase)
    implementation(projects.core.coroutines)
    implementation(projects.core.content.api)
    implementation(projects.feature.feed.api)
    implementation(projects.feature.feed.local)
    implementation(projects.feature.recommendation.api)

    // Pagging
    implementation(libs.room.paging)
    implementation(libs.androidx.pagging.ktx)

    implementation(libs.gson)

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
}
