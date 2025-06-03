package com.core.networks.datasource.prod

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val prefs: SharedPreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = prefs.getString(PREF_KEY_ACCESS_TOKEN, null)
        return if (!token.isNullOrEmpty()) {
            val newRequest = original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(original)
        }
    }

    companion object {
        private const val PREF_KEY_ACCESS_TOKEN = "pref_access_token"
    }
}
