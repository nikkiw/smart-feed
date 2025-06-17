plugins {
    alias(libs.plugins.smart.feed.android.library)
    alias(libs.plugins.smart.feed.android.library.jacoco)
}

android {
    namespace = "com.core.database"

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    defaultConfig {
        testInstrumentationRunner = "com.core.database.HiltCustomTestRunner"
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

    // Sqlite
    implementation(libs.sqlite.ktx)
    implementation(libs.sqlite.bundled)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.runtime.android)
    implementation(libs.room.common)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Pagging
    implementation(libs.room.paging)
    implementation(libs.androidx.pagging.ktx)

    implementation(libs.gson)

    // Testing dependencies
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.google.truth)
}