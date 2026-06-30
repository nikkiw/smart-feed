package com.ndev.android.smart.feed.startup

fun interface StartupErrorReporter {
    fun reportStartupFailure(error: Throwable)
}
