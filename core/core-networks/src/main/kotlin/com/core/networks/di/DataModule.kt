package com.core.networks.di

import android.content.Context
import com.core.networks.auth.AuthApiService
import com.core.networks.auth.AuthRepo
import com.core.networks.auth.AuthRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors
import javax.inject.Qualifier
import javax.inject.Singleton


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class AuthPrefsName


@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class BaseAuthUrl

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @AuthPrefsName
    @Singleton
    fun provideAuthPrefsName() = "auth_prefs"

    @Provides
    @Singleton
    fun provideAuthApiService(@BaseAuthUrl baseUrl: String) : AuthApiService{
        return  Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepo(
        @ApplicationContext context: Context,
        api: AuthApiService, // Retrofit interface для refresh
        @AuthPrefsName prefsName: String
    ): AuthRepo {
        return AuthRepoImpl(
            context = context,
            api = api,
            prefsName = prefsName
        )
    }



}