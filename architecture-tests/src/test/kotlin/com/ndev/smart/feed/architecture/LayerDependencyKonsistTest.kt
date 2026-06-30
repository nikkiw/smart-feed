package com.ndev.smart.feed.architecture

import kotlin.test.Test

class LayerDependencyKonsistTest {
    @Test
    fun `domain layer stays framework and implementation independent`() {
        sourceFilesUnder("core/core-domain/src/main").assertNoImports(
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
                    "com.core.data.",
                    "com.core.database.",
                    "com.core.networks.",
                    "com.feature.",
                    "com.ndev.android.smart.feed.",
                ),
            reason = "core-domain must stay pure and must not depend on frameworks or implementation layers.",
        )
    }

    @Test
    fun `core domain does not own feed content contracts`() {
        sourceFilesUnder("core/core-domain/src/main").assertTextDoesNotContain(
            forbiddenSnippets =
                listOf(
                    "ContentItemRepository",
                    "ContentItemPreview",
                    "data class Query",
                    "enum class ContentItemsSortedType",
                    "interface GetContentItemUseCase",
                    "interface SyncContentUseCase",
                    "interface ContentFetchScheduleUseCase",
                ),
            reason = "Feed content contracts must be owned by feature/feed/api.",
        )
    }

    @Test
    fun `core domain does not own recommendation contracts`() {
        sourceFilesUnder("core/core-domain/src/main").assertTextDoesNotContain(
            forbiddenSnippets =
                listOf(
                    "data class Recommendation",
                    "interface RecommendationRepository",
                    "interface Recommender",
                    "interface RecommendForArticleUseCase",
                    "interface RecommendForUserUseCase",
                ),
            reason = "Recommendation contracts must be owned by feature/recommendation/api.",
        )
    }

    @Test
    fun `data layer does not depend on app or feature packages`() {
        sourceFilesUnder(
            "core/core-data/src/main",
            "core/core-data/src/dev",
            "core/core-data/src/prod",
        ).assertNoImports(
            forbiddenPrefixes =
                listOf(
                    "com.feature.",
                    "com.ndev.android.smart.feed.",
                ),
            reason = "core-data can orchestrate data sources but must not depend on app or feature UI.",
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
                    "com.core.analytics.api.",
                    "com.core.analytics.impl.",
                    "com.core.data.",
                    "com.core.networks.",
                    "com.ndev.android.smart.feed.",
                ),
            reason =
                "core-database is a Room aggregator. It may import storage-schema " +
                    "modules only, not feature API/impl, data, network, app, or UI.",
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
                    "com.core.data.",
                ),
            reason =
                "core-networks must remain a network adapter and not depend on " +
                    "persistence, data orchestration, app, or feature UI.",
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
                    "com.core.data.",
                ),
            reason =
                "image-glide must remain an image loading adapter and not depend " +
                    "on app, feature, persistence, network, or data orchestration.",
        )
    }
}
