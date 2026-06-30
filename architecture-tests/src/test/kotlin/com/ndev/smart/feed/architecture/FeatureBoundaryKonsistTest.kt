package com.ndev.smart.feed.architecture

import kotlin.test.Test

class FeatureBoundaryKonsistTest {
    @Test
    fun `feed component contracts do not import implementation or Android view details`() {
        val contractFiles =
            sourceFilesUnder("feature/feed/src/main")
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
            reason = "Feed component contracts must stay usable as feature API contracts before module split.",
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
    fun `feed view extensions do not reach stores reducers repositories or data implementations`() {
        sourceFilesUnder("feature/feed/src/main")
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
        sourceFilesUnder("feature/feed/src/main")
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
