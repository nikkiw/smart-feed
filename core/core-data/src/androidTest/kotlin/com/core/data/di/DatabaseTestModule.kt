package com.core.data.di

import android.content.Context
import androidx.room.Room
import com.core.data.work.FakeImageLoader
import com.core.database.AppDatabase
import com.core.database.AppDatabase.Companion.createTrigger
import com.core.database.di.DatabaseModule
import com.core.image.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object DatabaseTestModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.Companion.getTestDatabase(context)
    }

    @Provides
    @Singleton
    fun provideImageLoader(rec: FakeImageLoader): ImageLoader = rec
}


