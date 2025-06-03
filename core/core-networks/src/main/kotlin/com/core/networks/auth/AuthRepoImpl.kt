package com.core.networks.auth

import android.content.Context
import android.content.SharedPreferences
import com.core.networks.di.AuthPrefsName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit



class AuthRepoImpl @Inject constructor(
    @ApplicationContext context: Context,
    val api: AuthApiService, // Retrofit interface для refresh
    @AuthPrefsName prefsName: String = "auth_prefs"
) : AuthRepo {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    private var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(value) = prefs.edit { putString("access_token", value) }

    private var refreshToken: String?
        get() = prefs.getString("refresh_token", null)
        set(value) = prefs.edit { putString("refresh_token", value) }

    override fun getAccessToken(forceRefresh: Boolean): String {
        if (accessToken == null || forceRefresh) {
            synchronized(this) {
                if (accessToken == null || forceRefresh) {
                    refreshAccessToken()
                }
            }
        }
        return accessToken ?: throw SessionExpiredException("Not authenticated")
    }

    // Метод ручного сохранения токенов (например, после логина)
    override fun saveTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
    }

    override fun login() {
        TODO("Not yet implemented")
    }

    // Метод выхода
    override fun logout() {
        accessToken = null
        refreshToken = null
    }

    // Реальный refresh
    private fun refreshAccessToken() {
        val refresh = refreshToken
            ?: throw SessionExpiredException("No refresh token. Please login again.")

        val response = api.refreshToken(refresh).execute()

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.accessToken.isNotEmpty() && body.accessToken .isNotEmpty()) {
                saveTokens(body.accessToken, refresh)
            } else {
                logout()
                throw SessionExpiredException("Invalid refresh response. Please login again.")
            }
        } else {
            logout()
            throw SessionExpiredException("Refresh token expired or invalid. Please login again.")
        }
    }
}