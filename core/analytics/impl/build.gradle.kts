plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.core.analytics.impl"

    defaultConfig {
        testInstrumentationRunner = "com.core.analytics.impl.HiltCustomTestRunner"
    }

    packaging {
        resources {
            excludes += setOf("DebugProbesKt.bin", "META-INF/LICENSE", "META-INF/NOTICE")
            pickFirsts += "META-INF/*"
        }
    }
}

dependencies {
    api(projects.core.analytics.api)

    implementation(projects.core.analytics.local)
    implementation(projects.core.content.api)
    implementation(projects.core.coroutines)
    implementation(projects.feature.userprofile.api)

    androidTestImplementation(projects.core.coreDatabase)
    androidTestImplementation(projects.feature.recommendation.local)
    androidTestImplementation(libs.room.runtime)
    androidTestImplementation(libs.room.common)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.google.truth)
}
