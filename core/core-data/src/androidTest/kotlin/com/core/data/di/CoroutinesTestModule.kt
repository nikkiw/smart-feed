package com.core.data.di

import android.util.Log
import com.core.di.AppCoroutineExceptionHandler
import com.core.di.ApplicationScope
import com.core.di.CoroutinesModule
import com.core.di.DefaultDispatcher
import com.core.di.IoDispatcher
import com.core.di.MainDispatcher
import com.core.di.MainImmediateDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CoroutinesModule::class]
)
object CoroutinesTestModule {

    @OptIn(ExperimentalCoroutinesApi::class)
//    val testDispatcher = UnconfinedTestDispatcher()
    val testDispatcher = StandardTestDispatcher()

    @Provides
    @MainDispatcher
    fun providesMainDispatcher(): CoroutineDispatcher = testDispatcher

    @Provides
    @MainImmediateDispatcher
    fun providesMainImmediateDispatcher(): CoroutineDispatcher = testDispatcher

    @Provides
    @IoDispatcher
    fun providesIODispatcher(): CoroutineDispatcher = testDispatcher

    @Provides
    @DefaultDispatcher
    fun providesDefaultDispatcher(): CoroutineDispatcher = testDispatcher

    @Singleton
    @AppCoroutineExceptionHandler
    @Provides
    fun provideCoroutineExceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, exception ->
            Log.e(
                "AppCoroutinesTestModule",
                "coroutineExceptionHandler error: ${exception.localizedMessage}",
                exception
            )
        }
    }

    @Singleton
    @ApplicationScope
    @Provides
    fun providesCoroutineScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
        @AppCoroutineExceptionHandler coroutineExceptionHandler: CoroutineExceptionHandler
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + dispatcher + coroutineExceptionHandler)
    }
}