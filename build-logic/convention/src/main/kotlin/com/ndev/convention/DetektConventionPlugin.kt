package com.ndev.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

/**
 * Detekt convention plugin that configures static code analysis for Kotlin modules.
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.gitlab.arturbosch.detekt")

        configure<DetektExtension> {
            // Enable default rule overrides using config/detekt/detekt.yml
            this.buildUponDefaultConfig = true
            
            // Locate the detekt.yml config file in the root directory
            this.config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
            
            // Explicitly set source targets so that all files are linted
            this.source.setFrom(files(
                "src/main/kotlin",
                "src/test/kotlin",
                "src/androidTest/kotlin",
                "src/main/java",
                "src/test/java",
                "src/androidTest/java"
            ).filter { it.exists() })
        }
    }
}
