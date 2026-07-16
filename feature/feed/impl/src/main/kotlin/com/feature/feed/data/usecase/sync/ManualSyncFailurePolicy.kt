package com.feature.feed.data.usecase.sync

fun interface ManualSyncFailurePolicy {
    fun failureForNextAttempt(): Throwable?
}
