package com.ndev.convention

import com.android.build.gradle.LibraryExtension
import com.ndev.convention.common.Config
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure

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

            tasks.withType(Test::class.java).configureEach {
                jvmArgs("-XX:+EnableDynamicAgentLoading")
            }

            configureDependenciesHilt()
            configureDependenciesUnitTests()
            configureDependenciesAndroidTests()
            configureDependenciesCoreLibraryDesugaring()
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
                buildConfigField("String", "BUILD_ENV", "\"dev\"")
            }
            create("prod") {
                dimension = "environment"
                buildConfigField("String", "BUILD_ENV", "\"prod\"")
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

        // Configuration Kotlin
        project.configureKotlin()
    }
}