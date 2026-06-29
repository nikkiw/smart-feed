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
    fun `app depends on feed root public factory only`() {
        sourceFilesUnder("app/src/main").assertNoImports(
            forbiddenPrefixes = listOf("com.feature.feed.root.FeedRootComponentImpl"),
            reason =
                "App startup must depend on the FeedRootComponent public contract, " +
                    "not the implementation factory.",
        )
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
}
