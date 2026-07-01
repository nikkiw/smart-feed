package com.ndev.smart.feed.architecture

import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.readText
import kotlin.test.Test

class LayerDependencyKonsistTest {
    @Test
    fun `core modules do not depend on feature APIs except approved aggregators`() {
        val violations =
            sourceFilesUnder("core")
                .filter { it.relativePath.contains("/src/main/") }
                .flatMap { file ->
                    file.imports
                        .filter { it.startsWith("com.feature.") }
                        .filterNot { import -> file.isApprovedCoreToFeatureImport(import) }
                        .map { import -> "${file.relativePath} imports $import" }
                }

        check(violations.isEmpty()) {
            buildString {
                appendLine(
                    "Core modules must not depend on feature API/implementation packages. " +
                        "Only approved aggregator dependencies may point to feature local schemas.",
                )
                violations.forEach { appendLine("- $it") }
            }
        }
    }

    @Test
    fun `core build files do not depend on feature modules except approved aggregators`() {
        val coreBuildFiles =
            projectRoot.resolve("core")
                .toFile()
                .walkTopDown()
                .filter { it.isFile && it.name == "build.gradle.kts" }
                .map { it.toPath() }
                .toList()

        val violations =
            coreBuildFiles.flatMap { buildFile ->
                val relativePath = projectRoot.relativize(buildFile).invariantSeparatorsPathString
                buildFile.readText()
                    .lineSequence()
                    .mapIndexedNotNull { index, line ->
                        if (!line.isMainDependencyDeclaration()) return@mapIndexedNotNull null
                        val dependency = line.extractFeatureProjectAccessor() ?: return@mapIndexedNotNull null
                        if (relativePath.isApprovedCoreBuildFeatureDependency(dependency)) {
                            return@mapIndexedNotNull null
                        }
                        "$relativePath:${index + 1} depends on projects.feature.$dependency"
                    }
                    .toList()
            }

        check(violations.isEmpty()) {
            buildString {
                appendLine(
                    "Core Gradle modules must not depend on feature modules. " +
                        "Only approved aggregator dependencies may point to feature local schemas.",
                )
                violations.forEach { appendLine("- $it") }
            }
        }
    }

    @Test
    fun `common module stays framework and implementation independent`() {
        sourceFilesUnder("core/common/src/main").assertNoImports(
            forbiddenPrefixes =
                listOf(
                    "android.",
                    "androidx.",
                    "android.arch.",
                    "com.google.dagger.",
                    "dagger.",
                    "javax.inject.",
                    "retrofit2.",
                    "okhttp3.",
                    "com.arkivanov.decompose.",
                    "com.arkivanov.essenty.",
                    "com.arkivanov.mvikotlin.",
                    "com.core.database.",
                    "com.core.networks.",
                    "com.core.analytics.",
                    "com.core.connectivity.",
                    "com.core.lifecycle.",
                    "com.core.image.",
                    "com.core.paging.",
                    "com.core.coroutines.",
                    "com.feature.",
                    "com.ndev.android.smart.feed.",
                ),
            reason =
                "core-common must stay pure and must not depend on frameworks, " +
                    "app, feature, or other core modules.",
        )
    }

    @Test
    fun `database aggregator depends only on feature local storage schemas`() {
        sourceFilesUnder("core/core-database/src/main").assertNoImports(
            forbiddenPrefixes =
                listOf(
                    "com.feature.feed.impl.",
                    "com.feature.feed.root.",
                    "com.feature.feed.list.",
                    "com.feature.feed.master.",
                    "com.feature.feed.article.",
                    "com.feature.feed.recommendation.",
                    "com.feature.recommendation.api.",
                    "com.feature.recommendation.impl.",
                    "com.feature.userprofile.api.",
                    "com.feature.userprofile.impl.",
                    "com.core.analytics.api.",
                    "com.core.analytics.impl.",
                    "com.core.networks.",
                    "com.ndev.android.smart.feed.",
                ),
            reason =
                "core-database is a Room aggregator. It may import storage-schema " +
                    "modules only, not feature API/impl, network, app, or UI.",
        )
    }

    @Test
    fun `network layer does not depend on app feature database or data packages`() {
        sourceFilesUnder(
            "core/core-networks/src/main",
            "core/core-networks/src/dev",
            "core/core-networks/src/prod",
        ).assertNoImports(
            forbiddenPrefixes =
                listOf(
                    "com.feature.",
                    "com.ndev.android.smart.feed.",
                    "com.core.database.",
                ),
            reason =
                "core-networks must remain a network adapter and not depend on " +
                    "persistence, app, or feature UI.",
        )
    }

    @Test
    fun `image loading adapter does not depend on app feature database network or data packages`() {
        sourceFilesUnder("core/image-glide/src/main").assertNoImports(
            forbiddenPrefixes =
                listOf(
                    "com.feature.",
                    "com.ndev.android.smart.feed.",
                    "com.core.database.",
                    "com.core.networks.",
                ),
            reason =
                "image-glide must remain an image loading adapter and not depend " +
                    "on app, feature, persistence, or network code.",
        )
    }

    private fun SourceFile.isApprovedCoreToFeatureImport(import: String): Boolean =
        when {
            relativePath.startsWith("core/core-database/") ->
                import.startsWith("com.feature.feed.local.") ||
                    import.startsWith("com.feature.recommendation.local.") ||
                    import.startsWith("com.feature.userprofile.local.")

            // Existing analytics implementation coupling is tracked separately from paging cleanup.
            relativePath.startsWith("core/analytics/impl/") ->
                import == "com.feature.userprofile.domain.repository.UserProfileRepository"

            else -> false
        }

    private fun String.extractFeatureProjectAccessor(): String? {
        val marker = "projects.feature."
        val start = indexOf(marker)
        if (start == -1) return null
        return substring(start + marker.length)
            .takeWhile { it.isLetterOrDigit() || it == '.' }
    }

    private fun String.isMainDependencyDeclaration(): Boolean {
        val trimmed = trimStart()
        return trimmed.startsWith("api(") ||
            trimmed.startsWith("implementation(") ||
            trimmed.startsWith("compileOnly(") ||
            trimmed.startsWith("runtimeOnly(") ||
            trimmed.startsWith("ksp(")
    }

    private fun String.isApprovedCoreBuildFeatureDependency(dependency: String): Boolean =
        when {
            startsWith("core/core-database/") ->
                dependency == "feed.local" ||
                    dependency == "recommendation.local" ||
                    dependency == "userprofile.local"

            startsWith("core/analytics/impl/") -> dependency == "userprofile.api"

            else -> false
        }
}
