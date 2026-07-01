package com.ndev.convention

import com.android.build.api.dsl.ApplicationExtension
import com.ndev.convention.common.Config
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationWithHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("com.google.dagger.hilt.android")
            pluginManager.apply("smart.feed.detekt")
            pluginManager.apply("smart.feed.spotless")

            extensions.configure<ApplicationExtension> {
                configureAndroidApplication(this, target)
            }

            configureDependenciesHilt()
            configureDependenciesUnitTests()
            configureDependenciesAndroidTests()
            configureDependenciesCoreLibraryDesugaring()
        }
    }

    private fun configureAndroidApplication(
        extension: ApplicationExtension,
        project: Project,
    ) {
        extension.apply {
            compileSdk = Config.COMPILE_SDK
            defaultConfig {
                minSdk = Config.MIN_SDK
                targetSdk = Config.TARGET_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            // Конфигурация подписи
//            signingConfigs {
//                create("debug") {
////                    storeFile = project.file("debug.keystore")
//                    storeFile = project.file("${System.getProperty("user.home")}/.android/debug.keystore")
//                    storePassword = "android"
//                    keyAlias = "androiddebugkey"
//                    keyPassword = "android"
//                }
//            }

            flavorDimensions += "environment"
            productFlavors {
                create("dev") {
                    dimension = "environment"
                    // Всё, что специфично для dev
                    buildConfigField("String", "BUILD_ENV", "\"dev\"")
                    // при необходимости: applicationIdSuffix = ".dev"  — для application-плагина,
                    // но в library-плагине его обычно не ставят.
                }
                create("prod") {
                    dimension = "environment"
                    // Всё, что специфично для prod
                    buildConfigField("String", "BUILD_ENV", "\"prod\"")
                }
            }
            buildTypes {
                getByName("debug") {
                    isMinifyEnabled = false
                    // Здесь можно добавить общие debug-поля, если нужно
                    buildConfigField("String", "BUILD_VARIANT", "\"debug\"")
                    signingConfig = signingConfigs.getByName("debug")
                }
                getByName("release") {
                    isMinifyEnabled = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                    buildConfigField("String", "BUILD_VARIANT", "\"release\"")
                    // Для dev release тоже используем debug ключ
                    signingConfig = signingConfigs.getByName("debug")
                }
            }
            compileOptions {
                sourceCompatibility = Config.COMPILE_JAVA_VERSION
                targetCompatibility = Config.COMPILE_JAVA_VERSION
                isCoreLibraryDesugaringEnabled = true
            }
            buildFeatures {
                buildConfig = true
            }
            packaging {
                jniLibs.keepDebugSymbols += "**/libsqliteJni.so"
            }
            // viewBinding { isEnabled = true }
            // dataBinding { isEnabled = true }
            project.configureKotlin()
        }
    }
}
