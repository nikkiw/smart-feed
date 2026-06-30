package com.ndev.android.smart.feed

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.feature.feed.root.FeedRootComponent
import com.ndev.android.smart.feed.startup.AppStartupCoordinator
import com.ndev.android.smart.feed.ui.FeedRootViewHost
import com.ndev.android.smart.feed.ui.SystemBarsController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var rootFactory: FeedRootComponent.Factory

    @Inject
    lateinit var startupCoordinator: AppStartupCoordinator

    @Inject
    lateinit var systemBarsController: SystemBarsController

    @Inject
    lateinit var rootViewHost: FeedRootViewHost

    private lateinit var feedRootComponent: FeedRootComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        systemBarsController.configure(
            activity = this,
            restoreAfterProcessDeath = savedInstanceState != null,
        )

        setContentView(R.layout.main_activity)

        val container: FrameLayout = findViewById(R.id.content)
        feedRootComponent = rootFactory(defaultComponentContext())
        rootViewHost.attach(
            container = container,
            component = feedRootComponent,
            lifecycle = essentyLifecycle(),
        )
        startupCoordinator.attach(this)
    }
}
