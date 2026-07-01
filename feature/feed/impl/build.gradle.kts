plugins {
    alias(libs.plugins.smart.feed.android.feature)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.feature.feed"

//    packaging {
//        resources {
//            excludes += setOf("DebugProbesKt.bin", "META-INF/LICENSE", "META-INF/NOTICE")
//            pickFirsts += "META-INF/*"
//        }
//    }
}

dependencies {
    api(projects.feature.feed.api)

    implementation(projects.core.core)
    implementation(projects.core.analytics.api)
    implementation(projects.core.content.api)
    implementation(projects.core.coroutines)
    implementation(projects.core.coreDatabase)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreNetworks)
    implementation(projects.core.corePaging)
    implementation(projects.core.connectivity)
    implementation(projects.core.image.api)
    implementation(projects.feature.feed.local)
    implementation(projects.feature.recommendation.api)

    // Pagging
    implementation(libs.room.paging)
    implementation(libs.androidx.pagging.ktx)

    // Worker
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler.androidx)

    // Markdown
    implementation(libs.markwon.core)
    implementation(libs.nikkiw.android.ui.components)

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
}
