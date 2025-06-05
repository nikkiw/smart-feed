package com.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.core.database.content.ContentDao
import com.core.database.content.ContentDatabase
import com.core.database.content.ContentDatabase.Companion.createTrigger
import com.core.database.content.ContentTagsDao
import com.core.database.content.UpdatesMetaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ContentDatabase {
        val dbName = "app_database"

        val db = Room.databaseBuilder(
            context.applicationContext,
            ContentDatabase::class.java,
            dbName
        )
            .setDriver(BundledSQLiteDriver())
            .build()

        runBlocking {
            createTrigger(db)
        }
        return db
    }

    @Singleton
    @Provides
    fun provideContentDao(
        db: ContentDatabase
    ): ContentDao {
        return db.contentDao()
    }

    @Singleton
    @Provides
    fun provideUpdatesMetaDao(
        db: ContentDatabase
    ): UpdatesMetaDao {
        return db.updatesMetaDao()
    }

    @Singleton
    @Provides
    fun provideContentTagsDao(
        db: ContentDatabase
    ): ContentTagsDao {
        return db.contentTagsDao()
    }
}