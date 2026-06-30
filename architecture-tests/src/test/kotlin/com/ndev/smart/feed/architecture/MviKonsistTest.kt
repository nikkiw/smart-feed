package com.ndev.smart.feed.architecture

import kotlin.test.Test

class MviKonsistTest {
    @Test
    fun `mvi declarations live in store packages`() {
        val misplacedFiles =
            sourceFilesUnder("feature/feed/impl/src/main")
                .filter {
                    val name = it.fileName().removeSuffix(".kt")
                    name.endsWith("Store") || name.endsWith("StoreFactory") || name.endsWith("Reducer")
                }
                .filterNot { it.packageName.orEmpty().contains(".store") }

        check(misplacedFiles.isEmpty()) {
            misplacedFiles.joinToString(
                prefix = "MVI store declarations must live in .store packages:\n",
                separator = "\n",
            ) { it.relativePath }
        }
    }

    @Test
    fun `reducers stay pure`() {
        sourceFilesUnder("feature/feed/impl/src/main")
            .filter { it.relativePath.endsWith("Reducer.kt") }
            .assertNoImports(
                forbiddenPrefixes =
                    listOf(
                        "android.",
                        "androidx.",
                        "com.arkivanov.decompose.",
                        "com.core.domain.repository.",
                        "com.core.domain.usecase.",
                        "com.core.data.",
                        "dagger.",
                        "javax.inject.",
                        "kotlinx.coroutines.",
                    ),
                reason =
                    "Reducers must be pure state transformers without IO, DI, " +
                        "Android, Decompose, or coroutine dependencies.",
            )
    }

    @Test
    fun `view extensions do not import store internals`() {
        sourceFilesUnder("feature/feed/impl/src/main")
            .filter { it.relativePath.endsWith("ViewExt.kt") }
            .assertNoImports(
                forbiddenPrefixes = listOf("com.feature.feed.store."),
                reason = "View extensions must depend on component contracts, not store internals.",
            )
    }
}
