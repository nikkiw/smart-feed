plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.feature.userprofile.impl"

    defaultConfig {
        testInstrumentationRunner = "com.feature.userprofile.data.HiltCustomTestRunner"
    }

    packaging {
        resources {
            excludes += setOf("DebugProbesKt.bin", "META-INF/LICENSE", "META-INF/NOTICE")
            pickFirsts += "META-INF/*"
        }
    }
}

dependencies {
    api(projects.feature.userprofile.api)

    implementation(projects.core.content.api)
    implementation(projects.core.coroutines)
    implementation(projects.feature.recommendation.local)
    implementation(projects.feature.userprofile.local)

    androidTestImplementation(projects.core.analytics.local)
    androidTestImplementation(projects.core.common)
    androidTestImplementation(projects.core.coreDatabase)
    androidTestImplementation(projects.feature.feed.local)
    androidTestImplementation(libs.room.runtime)
    androidTestImplementation(libs.room.common)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.google.truth)
}
