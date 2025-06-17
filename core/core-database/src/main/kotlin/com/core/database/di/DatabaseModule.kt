package com.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.core.database.AppDatabase
import com.core.database.AppDatabase.Companion.createTrigger
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
    ): AppDatabase {
        val dbName = "app_database"

        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            dbName
        )
            .setDriver(BundledSQLiteDriver())
            .build()

        runBlocking {
            createTrigger(db)
        }
        return db
    }
}
