package com.ndev.android.smart.feed.ui

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SystemBarsController
@Inject
constructor() {
    fun configure(activity: ComponentActivity, restoreAfterProcessDeath: Boolean) {
        val window = activity.window
        if (restoreAfterProcessDeath) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        // Both bars are fully transparent — Compose handles padding and backgrounds itself
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
