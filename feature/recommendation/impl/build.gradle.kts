plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.feature.recommendation.impl"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += setOf("DebugProbesKt.bin", "META-INF/LICENSE", "META-INF/NOTICE")
            pickFirsts += "META-INF/*"
        }
    }
}

dependencies {
    api(projects.feature.recommendation.api)

    implementation(projects.core.content.api)
    implementation(projects.core.coroutines)
    implementation(projects.feature.feed.api)
    implementation(projects.feature.feed.local)
    implementation(projects.feature.recommendation.local)
    implementation(projects.feature.userprofile.api)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    testImplementation(libs.google.truth)

    androidTestImplementation(projects.core.coreNetworks)
    androidTestImplementation(projects.core.coreDatabase)
    androidTestImplementation(projects.core.analytics.local)
    androidTestImplementation(projects.feature.feed.local)
    androidTestImplementation(projects.feature.userprofile.api)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.google.truth)
}
