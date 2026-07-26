package com.ndev.android.smart.feed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import com.feature.feed.compose.FeedRootContent
import com.feature.feed.root.FeedRootComponent
import com.ndev.android.smart.feed.startup.AppStartupCoordinator
import com.ndev.android.smart.feed.ui.SystemBarsController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var rootFactory: FeedRootComponent.Factory

    @Inject
    lateinit var startupCoordinator: AppStartupCoordinator

    @Inject
    lateinit var systemBarsController: SystemBarsController

    private lateinit var feedRootComponent: FeedRootComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        systemBarsController.configure(
            activity = this,
            restoreAfterProcessDeath = savedInstanceState != null,
        )

        feedRootComponent = rootFactory(defaultComponentContext())
        setContent {
            FeedRootContent(component = feedRootComponent)
        }
        startupCoordinator.attach(this)
    }
}
