package com.ndev.smart.feed.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name
import kotlin.io.path.readText

data class SourceFile(
    val path: Path,
    val packageName: String?,
    val imports: List<String>,
    val text: String,
) {
    val relativePath: String = projectRoot.relativize(path).invariantSeparatorsPathString
}

private val projectRoot: Path =
    Path.of(
        checkNotNull(System.getProperty("smartFeed.rootDir")) {
            "smartFeed.rootDir test system property must point to the repository root."
        },
    )

fun sourceFilesUnder(vararg roots: String): List<SourceFile> =
    roots
        .map { projectRoot.resolve(it) }
        .filter(Files::exists)
        .flatMap(::sourceFilesFrom)

private fun sourceFilesFrom(root: Path): List<SourceFile> =
    Files.walk(root).use { stream ->
        stream
            .filter { it.extension == "kt" }
            .filter { !it.invariantSeparatorsPathString.contains("/build/") }
            .map(::parseSourceFile)
            .toList()
    }

private fun parseSourceFile(path: Path): SourceFile {
    val text = path.readText()
    val lines = text.lineSequence().map(String::trim).toList()
    return SourceFile(
        path = path,
        packageName =
            lines.firstOrNull { it.startsWith("package ") }
                ?.removePrefix("package ")
                ?.trim(),
        imports =
            lines
                .filter { it.startsWith("import ") }
                .map { it.removePrefix("import ").substringBefore(" as ").trim() },
        text = text,
    )
}

fun List<SourceFile>.assertNoImports(
    forbiddenPrefixes: List<String>,
    reason: String,
) {
    val violations =
        flatMap { file ->
            file.imports
                .filter { import -> forbiddenPrefixes.any(import::startsWith) }
                .map { import -> "${file.relativePath} imports $import" }
        }

    check(violations.isEmpty()) {
        buildString {
            appendLine(reason)
            violations.forEach { appendLine("- $it") }
        }
    }
}

fun List<SourceFile>.assertTextDoesNotContain(
    forbiddenSnippets: List<String>,
    reason: String,
) {
    val violations =
        flatMap { file ->
            forbiddenSnippets
                .filter { snippet -> file.text.contains(snippet) }
                .map { snippet -> "${file.relativePath} contains $snippet" }
        }

    check(violations.isEmpty()) {
        buildString {
            appendLine(reason)
            violations.forEach { appendLine("- $it") }
        }
    }
}

fun SourceFile.fileName(): String = path.name
