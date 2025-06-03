package com.ndev.convention

import com.android.build.gradle.LibraryExtension
import com.ndev.convention.common.Config
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryWithHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("com.google.dagger.hilt.android")

            extensions.configure<LibraryExtension> {
                configureAndroidLibrary(target)
            }

            configureDependenciesHilt()
            configureDependenciesUnitTests()
            configureDependenciesAndroidTests()
        }
    }

    private fun LibraryExtension.configureAndroidLibrary(project: Project) {
        compileSdk = Config.COMPILE_SDK

        defaultConfig {
            minSdk = Config.MIN_SDK
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            consumerProguardFiles("consumer-rules.pro")
        }

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
            }

            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                buildConfigField("String", "BUILD_VARIANT", "\"release\"")
            }
        }

        compileOptions {
            sourceCompatibility = Config.COMPILE_JAVA_VERSION
            targetCompatibility = Config.COMPILE_JAVA_VERSION
        }

        buildFeatures {
            buildConfig = true
        }

        // Конфигурация Kotlin
        project.configureKotlin()
    }
}