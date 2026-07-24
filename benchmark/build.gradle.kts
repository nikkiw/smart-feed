plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.ndev.android.smart.feed.benchmark"
    compileSdk = com.ndev.convention.common.Config.COMPILE_SDK

    defaultConfig {
        minSdk = com.ndev.convention.common.Config.MIN_SDK
        targetSdk = com.ndev.convention.common.Config.TARGET_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Workaround for non-rooted Huawei/EMUI devices which block the DROP_SHADER_CACHE
        // broadcast when the app is in a force-stopped state.
        testInstrumentationRunnerArguments["androidx.benchmark.dropShaders.throwOnFailure"] = "false"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
        }
        create("prod") {
            dimension = "environment"
        }
    }

    buildTypes {
        create("benchmark") {
            isDebuggable = false
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    compileOptions {
        sourceCompatibility = com.ndev.convention.common.Config.COMPILE_JAVA_VERSION
        targetCompatibility = com.ndev.convention.common.Config.COMPILE_JAVA_VERSION
    }
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.uiautomator)
}

androidComponents {
    beforeVariants {
        it.enable = it.buildType == "benchmark"
    }
}
