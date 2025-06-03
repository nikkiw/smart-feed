package com.core.networks.auth


class SessionExpiredException(message: String) : Exception(message)


interface AuthRepo {

    fun getAccessToken(forceRefresh: Boolean = false): String

    // Метод ручного сохранения токенов (например, после логина)
    fun saveTokens(access: String, refresh: String)

    fun login()

    // Метод выхода
    fun logout()

}