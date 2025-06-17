import com.ndev.convention.common.Config

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
    implementation(projects.core.coreNetworks)
    implementation(projects.core.coreDatabase)

    implementation(libs.concurrent.futures.ktx)

    // Pagging
    implementation(libs.room.paging)
    implementation(libs.androidx.pagging.ktx)

    implementation(libs.gson)

    // Worker
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler.androidx)

    // Testing dependencies
    // Unit tests
    testImplementation(libs.google.truth)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.test.core.ktx)


    // Instrumental tests
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.pagging.testing)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.google.truth)

}