plugins {
    alias(libs.plugins.smart.feed.android.application)
    alias(libs.plugins.smart.feed.android.application.jacoco)
}

android {
    namespace = com.config.Config.NAMESPACE

    defaultConfig {
        applicationId = com.config.Config.APPLICATION_ID

        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    debugImplementation(libs.leakcanary)

    implementation(projects.core.core)
    implementation(projects.core.coreNetworks)
    implementation(projects.core.coreDatabase)
    implementation(projects.core.coreData)
    implementation(projects.core.coreDomain)
    implementation(projects.core.imageGlide)
    implementation(projects.feature.feed)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.bundles.decompose.libs)

    // Worker
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}