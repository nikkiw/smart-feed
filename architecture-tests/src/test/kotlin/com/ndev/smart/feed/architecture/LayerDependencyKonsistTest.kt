package com.ndev.smart.feed.architecture

import kotlin.test.Test

class LayerDependencyKonsistTest {
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
}
