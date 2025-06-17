package com.core.observers

import androidx.lifecycle.ProcessLifecycleOwner
import com.core.di.ApplicationScope
import com.core.di.MainDispatcher
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Observes the application lifecycle and provides a mechanism to add
 * LifecycleObserver instances to the ProcessLifecycleOwner on the specified dispatcher.
 *
 * @constructor
 * Creates an instance of [AppLifecycleObserver].
 * @param applicationScope The CoroutineScope tied to the application's lifecycle.
 * @param mainDispatcher The CoroutineDispatcher on which the observer addition will run.
 */
@Singleton
class AppLifecycleObserver @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) {

    /**
     * Registers the given [LifecycleObserver] with the application's process lifecycle.
     *
     * Uses [applicationScope] and launches a coroutine on [mainDispatcher]
     * to add the observer to [ProcessLifecycleOwner].
     *
     * @param observer The [androidx.lifecycle.LifecycleObserver] instance
     *                 that will receive lifecycle callbacks.
     */
    fun addObserver(observer: androidx.lifecycle.LifecycleObserver) {
        applicationScope.launch(mainDispatcher) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
        }
    }
}
