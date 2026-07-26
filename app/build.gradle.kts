plugins {
    alias(libs.plugins.smart.feed.android.application)
    alias(libs.plugins.smart.feed.android.application.jacoco)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = com.ndev.convention.common.Config.NAMESPACE

    defaultConfig {
        applicationId = com.ndev.convention.common.Config.APPLICATION_ID

        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
            enableAndroidTestCoverage = false
            enableUnitTestCoverage = false
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    debugImplementation(libs.leakcanary)

    implementation(projects.core.common)
    implementation(projects.core.coreNetworks)
    implementation(projects.core.coreDatabase)
    implementation(projects.core.coroutines)
    implementation(projects.core.lifecycle)
    implementation(projects.core.connectivity)
    implementation(projects.core.analytics.impl)
    implementation(projects.feature.feed.impl)
    implementation(projects.feature.recommendation.impl)
    implementation(projects.feature.userprofile.impl)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.decompose)

    // Worker
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)

    // Needed for macrobenchmark on non-rooted devices: registers ProfileInstallReceiver
    // to handle the DROP_SHADER_CACHE broadcast sent before each benchmark iteration.
    implementation(libs.androidx.profileinstaller)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
