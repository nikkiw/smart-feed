plugins {
    alias(libs.plugins.smart.feed.android.feature)
    alias(libs.plugins.smart.feed.android.library.jacoco)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.feature.feed"

    defaultConfig {
        testInstrumentationRunner = "com.feature.feed.HiltCustomTestRunner"
    }

    buildFeatures {
        compose = true
    }

//    packaging {
//        resources {
//            excludes += setOf("DebugProbesKt.bin", "META-INF/LICENSE", "META-INF/NOTICE")
//            pickFirsts += "META-INF/*"
//        }
//    }
}

dependencies {
    api(projects.feature.feed.api)

    implementation(projects.core.common)
    implementation(projects.core.analytics.api)
    implementation(projects.core.content.api)
    implementation(projects.core.coroutines)
    implementation(projects.core.coreDatabase)
    implementation(projects.core.coreNetworks)
    implementation(projects.core.connectivity)
    implementation(projects.feature.feed.local)
    implementation(projects.feature.recommendation.api)

    implementation(libs.decompose.extensions.compose)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.extensions.coroutines)

    // Pagging
    implementation(libs.room.paging)
    implementation(libs.androidx.pagging.ktx)
    implementation(libs.androidx.pagging.compose)

    // Worker
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler.androidx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.markdown.renderer)
    implementation(libs.markdown.renderer.m3)
    implementation(libs.markdown.renderer.coil3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Unit test
    testImplementation(libs.google.truth)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.test.core.ktx)

    // Instrumental tests
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.pagging.testing)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.google.truth)
    androidTestImplementation(projects.core.image.api)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
