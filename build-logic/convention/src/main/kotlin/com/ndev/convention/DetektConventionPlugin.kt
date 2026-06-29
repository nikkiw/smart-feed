package com.ndev.convention

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/**
 * Detekt convention plugin that configures static code analysis for Kotlin modules.
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            val configDir = rootProject.layout.projectDirectory.dir("config/detekt")
            val commonConfig = configDir.file("detekt-common.yml")
            val productionConfig = configDir.file(productionConfigName())
            val testConfig = configDir.file("detekt-test.yml")
            val androidTestConfig = configDir.file("detekt-android-test.yml")

            configure<DetektExtension> {
                buildUponDefaultConfig = true
                parallel = true
                config.setFrom(files(commonConfig, productionConfig))
                source.setFrom(files(productionSources() + testSources() + androidTestSources()))
                basePath = rootProject.projectDir.absolutePath
            }

            registerLayeredDetektTask(
                taskName = "detektMain",
                description = "Run Detekt for production sources with the module layer profile.",
                sourceDirs = productionSources(),
                configFiles = listOf(commonConfig.asFile, productionConfig.asFile),
            )
            registerLayeredDetektTask(
                taskName = "detektTest",
                description = "Run Detekt for JVM unit tests with the test profile.",
                sourceDirs = testSources(),
                configFiles = listOf(commonConfig.asFile, testConfig.asFile),
            )
            registerLayeredDetektTask(
                taskName = "detektAndroidTest",
                description = "Run Detekt for Android instrumentation tests with the android-test profile.",
                sourceDirs = androidTestSources(),
                configFiles = listOf(commonConfig.asFile, androidTestConfig.asFile),
            )

            tasks.withType<Detekt>().configureEach {
                jvmTarget = "17"
                basePath = rootProject.projectDir.absolutePath
                if (name == "detekt") {
                    setSource(files())
                }
            }

            tasks.named("detekt") {
                dependsOn("detektMain", "detektTest", "detektAndroidTest")
            }
        }
    }

    private fun Project.registerLayeredDetektTask(
        taskName: String,
        description: String,
        sourceDirs: List<String>,
        configFiles: List<java.io.File>,
    ) {
        tasks.register<Detekt>(taskName) {
            group = "verification"
            this.description = description
            buildUponDefaultConfig = true
            parallel = true
            setSource(files(sourceDirs.map(::file).filter { it.exists() }))
            config.setFrom(files(configFiles.filter { it.exists() }))
            exclude("**/build/**")
        }
    }

    private fun Project.productionConfigName(): String = when (path) {
        ":core:core-domain" -> "detekt-domain.yml"
        ":core:core",
        ":core:core-data",
        ":core:core-database",
        ":core:core-networks",
        ":core:image-glide",
        -> "detekt-data.yml"
        else -> "detekt-ui.yml"
    }

    private fun productionSources(): List<String> = listOf(
        "src/main/kotlin",
        "src/main/java",
        "src/dev/kotlin",
        "src/dev/java",
        "src/prod/kotlin",
        "src/prod/java",
    )

    private fun testSources(): List<String> = listOf(
        "src/test/kotlin",
        "src/test/java",
    )

    private fun androidTestSources(): List<String> = listOf(
        "src/androidTest/kotlin",
        "src/androidTest/java",
    )
}
