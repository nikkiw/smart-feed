package com.core.data.work


import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

object HiltWorkerFactoryForTest {

    fun create(context: Context): WorkerFactory {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            HiltWorkerFactoryEntryPoint::class.java
        )
        return object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ) = entryPoint.hiltWorkerFactory().createWorker(
                appContext,
                workerClassName,
                workerParameters
            )
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun hiltWorkerFactory(): HiltWorkerFactory
    }
}
