package com.ndev.convention

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.ndev.convention.common.configureJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

private const val COVERAGE_BUILD_TYPE = "debug"

class AndroidLibraryJacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "jacoco")

            val androidExtension = extensions.getByType<LibraryExtension>()

            androidExtension.buildTypes.configureEach {
                val coverageEnabled = name == COVERAGE_BUILD_TYPE
                enableAndroidTestCoverage = coverageEnabled
                enableUnitTestCoverage = coverageEnabled
            }

            configureJacoco(extensions.getByType<LibraryAndroidComponentsExtension>())
        }
    }
}