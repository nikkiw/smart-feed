package com.ndev.android.smart.feed.startup

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.core.domain.service.AppBootstrapper
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityScoped
class AppStartupCoordinator
    @Inject
    constructor(
        private val appBootstrapper: AppBootstrapper,
        private val startupErrorReporter: StartupErrorReporter,
    ) : DefaultLifecycleObserver {
        private var bootstrapJob: Job? = null

        fun attach(owner: LifecycleOwner) {
            owner.lifecycle.addObserver(this)
        }

        override fun onStart(owner: LifecycleOwner) {
            if (bootstrapJob != null) return

            val exceptionHandler =
                CoroutineExceptionHandler { _, throwable ->
                    startupErrorReporter.reportStartupFailure(throwable)
                }

            bootstrapJob =
                owner.lifecycleScope.launch(exceptionHandler) {
                    appBootstrapper.bootstrap()
                }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
        }
    }
