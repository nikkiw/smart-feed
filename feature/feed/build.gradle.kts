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
    implementation(projects.core.core)
    implementation(projects.core.coreDomain)


    // Pagging
    implementation(libs.androidx.pagging.ktx)
//
//    implementation(libs.gson)
//
//    // Testing dependencies
//    androidTestImplementation(libs.room.testing)
}