package com.core.networks.datasource.dev

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DevStaticJsonTestNetworkDataSourceTest {
    private val context = mockk<Context>(relaxed = true)
    private val dataSource = DevStaticJsonTestNetworkDataSource(context, isInternetAvailable = { false })

    @Test
    fun `getUpdates fails offline instead of loading bundled assets`() = runTest {
        val result = dataSource.getUpdates(since = "1970-01-01T00:00:00Z", limit = 100)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("No internet connection")
    }

    @Test
    fun `getContentById fails offline instead of loading bundled assets`() = runTest {
        val result = dataSource.getContentById(type = "article", id = "article-1")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("No internet connection")
    }
}
