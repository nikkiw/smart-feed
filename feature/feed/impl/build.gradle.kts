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
    implementation(projects.core.coreDomain)
    implementation(projects.core.corePaging)
    implementation(projects.core.connectivity)
    implementation(projects.core.image.api)

    // Pagging
    implementation(libs.androidx.pagging.ktx)

    // Markdown
    implementation(libs.markwon.core)
    implementation(libs.nikkiw.android.ui.components)

    // Unit test
    testImplementation(libs.google.truth)
}
