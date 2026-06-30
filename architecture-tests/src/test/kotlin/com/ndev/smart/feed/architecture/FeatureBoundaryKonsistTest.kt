package com.ndev.smart.feed.architecture

import kotlin.test.Test

class FeatureBoundaryKonsistTest {
    @Test
    fun `feed api module exposes contracts without depending on implementation`() {
        val apiFiles = sourceFilesUnder("feature/feed/api/src/main")
        val implFiles = sourceFilesUnder("feature/feed/impl/src/main")

        check(apiFiles.isNotEmpty()) {
            "feature/feed/api must exist and contain public feed contracts after module split."
        }
        check(implFiles.isNotEmpty()) {
            "feature/feed/impl must exist and contain feed implementation after module split."
        }

        apiFiles.assertNoImports(
            forbiddenPrefixes =
                listOf(
                    "com.feature.feed.impl.",
                    "android.view.",
                    "android.widget.",
                    "androidx.recyclerview.",
                    "com.google.android.material.",
                    "io.noties.markwon.",
                    "com.bumptech.glide.",
                    "com.image.glide.",
                    "com.core.data.",
                    "com.core.database.",
                    "com.core.networks.",
                ),
            reason = "Feed API module must expose contracts without implementation or UI dependencies.",
        )

        apiFiles
            .filter { it.fileName().removeSuffix(".kt").endsWith("Impl") }
            .let { implNamedApiFiles ->
                check(implNamedApiFiles.isEmpty()) {
                    buildString {
                        appendLine("Feed API module must not contain implementation classes.")
                        implNamedApiFiles.forEach { appendLine("- ${it.relativePath}") }
                    }
                }
            }
    }

    @Test
    fun `feed component contracts do not import implementation or Android view details`() {
        val contractFiles =
            sourceFilesUnder("feature/feed/api/src/main")
                .filter { it.relativePath.endsWith("Component.kt") }

        contractFiles.assertNoImports(
            forbiddenPrefixes =
                listOf(
                    "android.view.",
                    "android.widget.",
                    "androidx.recyclerview.",
                    "com.google.android.material.",
                    "io.noties.markwon.",
                    "com.bumptech.glide.",
                    "com.image.glide.",
                    "com.core.data.",
                    "com.core.database.",
                    "com.core.networks.",
                ),
            reason = "Feed component contracts must stay usable as feature API contracts.",
        )

        val implImports =
            contractFiles.flatMap { file ->
                file.imports
                    .filter { it.substringAfterLast('.').endsWith("Impl") }
                    .map { "${file.relativePath} imports implementation type $it" }
            }
        check(implImports.isEmpty()) {
            implImports.joinToString(
                prefix = "Component contracts must not import implementation classes:\n",
                separator = "\n",
            )
        }
    }

    @Test
    fun `feed local module owns Room schema without depending on implementation`() {
        val localFiles = sourceFilesUnder("feature/feed/local/src/main")

        check(localFiles.isNotEmpty()) {
            "feature/feed/local must exist and own feed Room entities and DAOs."
        }

        localFiles.assertNoImports(
            forbiddenPrefixes =
                listOf(
                    "com.feature.feed.impl.",
                    "com.core.database.",
                    "com.core.data.",
                    "com.core.networks.",
                    "com.ndev.android.smart.feed.",
                    "android.view.",
                    "android.widget.",
                    "androidx.recyclerview.",
                    "androidx.work.",
                    "retrofit2.",
                    "okhttp3.",
                    "io.noties.markwon.",
                ),
            reason =
                "feature:feed:local is a storage schema module. It may use Room annotations " +
                    "and feature API types, but must not depend on runtime implementation, app, " +
                    "network, WorkManager, or UI code.",
        )
    }

    @Test
    fun `feed view extensions do not reach stores reducers repositories or data implementations`() {
        sourceFilesUnder("feature/feed/impl/src/main")
            .filter { it.relativePath.endsWith("ViewExt.kt") }
            .assertNoImports(
                forbiddenPrefixes =
                    listOf(
                        "com.core.data.",
                        "com.core.database.",
                        "com.core.networks.",
                        "com.arkivanov.mvikotlin.",
                    ),
                reason =
                    "ViewExt files render component state and dispatch component " +
                        "events; they must not reach data or MVI internals directly.",
            )
    }

    @Test
    fun `main activity stays thin app shell`() {
        val violations =
            sourceFilesUnder("app/src/main")
                .filter { it.relativePath.endsWith("MainActivity.kt") }
                .flatMap { file ->
                    file.imports.mapNotNull { import ->
                        val reason =
                            when {
                                import.hasImplementationType() ->
                                    "imports an implementation type"
                                import.startsWith("com.core.") ->
                                    "imports core/domain/data code directly"
                                import.isInfrastructureImport() ->
                                    "imports infrastructure API directly"
                                else -> null
                            }
                        reason?.let { "${file.relativePath} imports $import ($it)" }
                    }
                }

        check(violations.isEmpty()) {
            buildString {
                appendLine(
                    "MainActivity must stay a thin Android shell and delegate feature, " +
                        "startup, domain, and infrastructure work to focused collaborators.",
                )
                violations.forEach { appendLine("- $it") }
            }
        }
    }

    @Test
    fun `feed coordination does not cast contracts to implementations`() {
        sourceFilesUnder("feature/feed/impl/src/main")
            .filter { it.relativePath.endsWith("ComponentImpl.kt") }
            .assertTextDoesNotContain(
                forbiddenSnippets =
                    listOf(
                        " is FeedListComponentImpl",
                        " as FeedListComponentImpl",
                        "import com.feature.feed.list.FeedListComponentImpl",
                    ),
                reason =
                    "Parent components must coordinate through public child component " +
                        "contracts, not implementation casts.",
            )
    }

    private fun String.hasImplementationType(): Boolean = substringAfterLast('.').endsWith("Impl") || contains(".impl.")

    private fun String.isInfrastructureImport(): Boolean =
        startsWith("androidx.work.") ||
            startsWith("androidx.room.") ||
            startsWith("retrofit2.") ||
            startsWith("okhttp3.")
}
