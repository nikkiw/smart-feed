package com.ndev.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {

        kotlinOptions {
            jvmTarget = "17"

            // Дополнительные опции компилятора
//            freeCompilerArgs = freeCompilerArgs + listOf(
//                "-opt-in=kotlin.RequiresOptIn",
//                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
//            )
        }
    }
}
