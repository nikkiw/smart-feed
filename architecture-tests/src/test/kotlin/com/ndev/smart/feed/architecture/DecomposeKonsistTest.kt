package com.ndev.smart.feed.architecture

import kotlin.test.Test

class DecomposeKonsistTest {
    @Test
    fun `component implementations use ComponentImpl suffix`() {
        val invalidFiles =
            sourceFilesUnder("feature/feed/impl/src/main")
                .filter { it.relativePath.endsWith("ComponentImpl.kt") }
                .filterNot { it.fileName().removeSuffix(".kt").endsWith("ComponentImpl") }

        check(invalidFiles.isEmpty()) {
            invalidFiles.joinToString(
                prefix = "Component implementation files must use ComponentImpl suffix:\n",
                separator = "\n",
            ) { it.relativePath }
        }
    }

    @Test
    fun `each ComponentImpl has a matching Component contract file`() {
        val files =
            sourceFilesUnder(
                "feature/feed/api/src/main",
                "feature/feed/impl/src/main",
            )
        val contractNames =
            files
                .filter { it.relativePath.endsWith("Component.kt") }
                .map { it.fileName().removeSuffix(".kt") }
                .toSet()

        val missingContracts =
            files
                .filter { it.relativePath.startsWith("feature/feed/impl/") }
                .filter { it.relativePath.endsWith("ComponentImpl.kt") }
                .map { it.fileName().removeSuffix("Impl.kt") }
                .filterNot { it in contractNames }

        check(missingContracts.isEmpty()) {
            missingContracts.joinToString(
                prefix = "Component implementations without matching contracts:\n",
                separator = "\n",
            )
        }
    }
}
