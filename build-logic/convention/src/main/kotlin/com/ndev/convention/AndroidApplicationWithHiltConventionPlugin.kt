package com.ndev.convention

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.ndev.convention.common.Config
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Плагин-конвенция для Android Application с Hilt.
 * В зависимости от версии Android Gradle Plugin (AGP), может использоваться либо
 * AppExtension (для AGP < 7), либо BaseAppModuleExtension (AGP 7+).
 */
class AndroidApplicationWithHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // 1) Применяем плагины
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.android")
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("com.google.dagger.hilt.android")

            // 2) Конфигурируем Android Application Extension
            //    Для AGP 7 и выше: BaseAppModuleExtension
            //    Для более старых AGP: AppExtension
            extensions.configure<BaseAppModuleExtension> {
                configureAndroidApplication(this, target)
            }

            // 3) Дополнительные зависимости (Hilt, тесты и т.д.)
            configureDependenciesHilt()
            configureDependenciesUnitTests()
            configureDependenciesAndroidTests()
        }
    }

    private fun configureAndroidApplication(
        extension: BaseAppModuleExtension,
        project: Project
    ) {
        extension.apply {
            compileSdkVersion(Config.COMPILE_SDK)

            defaultConfig {
                minSdk = Config.MIN_SDK
                targetSdk = Config.TARGET_SDK

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

            // Если нужен ViewBinding или DataBinding, раскомментируйте, например:
            // viewBinding { isEnabled = true }
            // dataBinding { isEnabled = true }

            // Конфигурация Kotlin (если у вас есть какой-то обобщённый метод configureKotlin)
            project.configureKotlin()
        }
    }


}
