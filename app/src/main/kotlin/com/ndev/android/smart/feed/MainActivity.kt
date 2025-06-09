package com.ndev.android.smart.feed

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils.calculateLuminance
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.android.DefaultViewContext
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.core.domain.repository.ContentItemRepository
import com.feature.feed.root.FeedRootComponent
import com.feature.feed.root.FeedRootComponentImpl.FeedRootComponentFactory
import com.feature.feed.root.FeedRootComponentView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var contentItemRepository: ContentItemRepository

    @Inject
    lateinit var rootFactory: FeedRootComponentFactory

    private lateinit var feedRootComponent: FeedRootComponent


    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        setupStatusBarEdgeToEdge(window, findViewById(android.R.id.content))

        setContentView(R.layout.main_activity)

        val container: FrameLayout = findViewById(R.id.content)

        // 1. Decompose wrapper: create ViewContext, throw in parent and lifecycle
        feedRootComponent = rootFactory(defaultComponentContext())

        // 2. Create and add View
        val viewContext = DefaultViewContext(
            parent = container,
            lifecycle = essentyLifecycle()
        )
        val rootView = viewContext.FeedRootComponentView(feedRootComponent)
        container.removeAllViews()
        container.addView(rootView)

        lifecycleScope.launch {

            contentItemRepository.apply {
                if (isEmpty()) {
                    syncContent()
                }
            }
        }
    }

    private fun setupStatusBarEdgeToEdge(window: Window, rootView: View) {
        // 1) Drop content under the system panels (edge-to-edge basis)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2) Allow yourself to draw the background of system bars
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


        // 3) Getting the colors from the theme
        val bgColor = TypedValue().run {
            theme.resolveAttribute(android.R.attr.colorBackground, this, true)
            data
        }
        // 4) Set the color of icons for the status bar and navigation bar
        WindowCompat.getInsetsController(window, rootView).apply {
            val lightBg = calculateLuminance(bgColor) > 0.5
            isAppearanceLightStatusBars = lightBg
            isAppearanceLightNavigationBars = lightBg

        }

        // 5) Customize indentation for edge-to-edge while maintaining content interactivity
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
            )
            view.setBackgroundColor(bgColor)
            // Adjust padding to avoid overlap
            val bottom = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                insets.bottom
            } else {
                0
            }
            view.setPadding(insets.left, insets.top, insets.right, bottom)
            WindowInsetsCompat.CONSUMED
        }

        // 6) To ensure correct rendering under cutouts (screen cutouts)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
}