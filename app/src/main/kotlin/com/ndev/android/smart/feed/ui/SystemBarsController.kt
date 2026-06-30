package com.ndev.android.smart.feed.ui

import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils.calculateLuminance
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SystemBarsController
    @Inject
    constructor() {
        fun configure(
            activity: AppCompatActivity,
            restoreAfterProcessDeath: Boolean,
        ) {
            val window = activity.window
            val rootView: View = activity.findViewById(android.R.id.content)

            if (restoreAfterProcessDeath && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }

            activity.enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            val bgColor =
                TypedValue().run {
                    activity.theme.resolveAttribute(android.R.attr.colorBackground, this, true)
                    data
                }

            WindowCompat.getInsetsController(window, rootView).apply {
                val lightBg = calculateLuminance(bgColor) > 0.5
                isAppearanceLightStatusBars = lightBg
                isAppearanceLightNavigationBars = lightBg
            }

            ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
                val insets =
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() or
                            WindowInsetsCompat.Type.displayCutout(),
                    )
                val bottom =
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        insets.bottom
                    } else {
                        0
                    }

                view.setBackgroundColor(bgColor)
                view.setPadding(insets.left, insets.top, insets.right, bottom)
                WindowInsetsCompat.CONSUMED
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }
